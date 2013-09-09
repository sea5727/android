package org.androidtown.nfc.writer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;

/**
 * NFC �±׿� �����͸� ���� ����� ���� �� �� �ֽ��ϴ�.
 * 
 * @author Mike
 */
public class MainActivity extends Activity {
	public static final String TAG = "MainActivity";

	private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private TextView mText;

	public static final int TYPE_TEXT = 1;
	public static final int TYPE_URI = 2;

	public static String SOURCE_TAG_MESSAGE = "Hello, this is android town.";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = NfcAdapter.getDefaultAdapter(this);

        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate() called.");


        mText = (TextView) findViewById(R.id.text);
        if (mAdapter == null) {
        	mText.setText("����ϱ� ���� NFC�� Ȱ��ȭ�ϼ���.");
        } else {
        	mText.setText("NFC �±׸� ��ĵ�ϼ���. \n\n��ĵ�Ǵ� TAG�� �� ������ :\n " + SOURCE_TAG_MESSAGE);
        }

        Intent targetIntent = new Intent(this, MainActivity.class);
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mPendingIntent = PendingIntent.getActivity(this, 0, targetIntent, 0);


        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }

        mFilters = new IntentFilter[] {
                ndef,
        };

        mTechLists = new String[][] { new String[] { NfcF.class.getName() } };


        Intent passedIntent = getIntent();
        if (passedIntent != null) {
        	String action = passedIntent.getAction();
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            	processTag(passedIntent);
            }
        }

    }

    /**
     * Enable NFC adapter to read mode
     */
    public void onResume() {
        super.onResume();

        if (mAdapter != null) {
        	mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
    }

    /**
     * Disable NFC adapter from read mode
     */
    public void onPause() {
        super.onPause();

        if (mAdapter != null) {
        	mAdapter.disableForegroundDispatch(this);
        }
    }

    /**
     * Passed intent to the single top activity
     */
    public void onNewIntent(Intent passedIntent) {
    	Log.d(TAG, "onNewIntent() called.");

    	if (passedIntent != null) {
        	processTag(passedIntent);
        }
    }

    /**
     * Process detected tag
     *
     * @param passedIntent
     */
    private void processTag(Intent passedIntent) {
    	Log.d(TAG, "processTag() called.");

    	Tag detectedTag = passedIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    	NdefMessage message = createTagMessage(SOURCE_TAG_MESSAGE, TYPE_TEXT);
    	writeTag(message, detectedTag);

    }

    /**
     * Write a NdefMessage to the detected tag
     *
     * @param message
     * @param tag
     * @return
     */
    public boolean writeTag(NdefMessage message, Tag tag) {
    	int size = message.toByteArray().length;
    	try {
    		Ndef ndef = Ndef.get(tag);
    		if (ndef != null) {
    			ndef.connect();
    			if (!ndef.isWritable()) {
    				return false;
    			}

    			if (ndef.getMaxSize() < size) {
    				return false;
    			}

    			ndef.writeNdefMessage(message);
    		} else {
    			Toast.makeText(this, "���˵��� ���� �±��̹Ƿ� ���� �����ϰ� �����͸� ���ϴ�.", Toast.LENGTH_SHORT).show();

    			NdefFormatable formatable = NdefFormatable.get(tag);
    		    if (formatable != null) {
    		        try {
    		            formatable.connect();
    		            formatable.format(message);
    		        } catch(IOException ex) {
    		            ex.printStackTrace();
    		        }
    		    }
    		    
    			return false;
    		}
    	} catch(Exception ex) {
    		ex.printStackTrace();

    		return false;
    	}

    	return true;
    }

    /**
     * Show the detected tag contents
     *
     * @param mMessage
     * @return
     */
    private int showTag(NdefMessage mMessage) {
        List<ParsedRecord> records = NdefMessageParser.parse(mMessage);
        final int size = records.size();
        mText.append("\n");
        for (int i = 0; i < size; i++) {
            ParsedRecord record = records.get(i);

            int recordType = record.getType();
            String recordStr = "";
            if (recordType == ParsedRecord.TYPE_TEXT) {
            	recordStr = "TEXT : " + ((TextRecord) record).getText() + "\n";
            } else if (recordType == ParsedRecord.TYPE_URI) {
            	recordStr = "URI : " + ((UriRecord) record).getUri().toString() + "\n";
            }

            Log.d(TAG, "record string : " + recordStr);

            mText.append(recordStr);
            mText.invalidate();
        }

        return size;
    }

    /**
     * Create a new tag message
     *
     * @param msg
     * @param type
     * @return
     */
    private NdefMessage createTagMessage(String msg, int type) {
    	NdefRecord[] records = new NdefRecord[1];

    	if (type == TYPE_TEXT) {
    		records[0] = createTextRecord(msg, Locale.KOREAN, true);
    	} else if (type == TYPE_URI){
    		records[0] = createUriRecord(msg.getBytes());
    	}

    	NdefMessage mMessage = new NdefMessage(records);

    	return mMessage;
    }


    private NdefRecord createTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        final byte[] langBytes = locale.getLanguage().getBytes(Charsets.US_ASCII);
        final Charset utfEncoding = encodeInUtf8 ? Charsets.UTF_8 : Charset.forName("UTF-16");
        final byte[] textBytes = text.getBytes(utfEncoding);
        final int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        final char status = (char) (utfBit + langBytes.length);
        final byte[] data = Bytes.concat(new byte[] {(byte) status}, langBytes, textBytes);
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    private NdefRecord createUriRecord(byte[] data) {
        return new NdefRecord(NdefRecord.TNF_ABSOLUTE_URI, NdefRecord.RTD_URI, new byte[0], data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}