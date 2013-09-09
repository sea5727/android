package org.androidtown.multimemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.androidtown.multimemo.common.TitleBackgroundButton;
import org.androidtown.multimemo.common.TitleBitmapButton;
import org.androidtown.multimemo.db.MemoDatabase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * �� �޸� / �޸� ���� ��Ƽ��Ƽ
 *
 * @author Mike
 * @date 2011-07-01
 */
public class MemoInsertActivity extends Activity {

	public static final String TAG = "MemoInsertActivity";

	TitleBitmapButton mPhotoBtn;
	TitleBitmapButton mVideoBtn;
	TitleBitmapButton mVoiceBtn;
	TitleBitmapButton mHandwritingBtn;

	EditText mMemoEdit;
	ImageView mPhoto;

	String mMemoMode;
	String mMemoId;
	String mMemoDate;

	String mMediaPhotoId;
	String mMediaPhotoUri;
	String mMediaVideoId;
	String mMediaVideoUri;
	String mMediaVoiceId;
	String mMediaVoiceUri;
	String mMediaHandwritingId;
	String mMediaHandwritingUri;

	String tempPhotoUri;
	String tempVideoUri;
	String tempVoiceUri;
	String tempHandwritingUri;

	String mDateStr;
	String mMemoStr;

	Bitmap resultPhotoBitmap;


	Bitmap resultHandwritingBitmap;

	boolean isPhotoCaptured;
	boolean isVideoRecorded;
	boolean isVoiceRecorded;
	boolean isHandwritingMade;

	boolean isPhotoFileSaved;
	boolean isVideoFileSaved;
	boolean isVoiceFileSaved;
	boolean isHandwritingFileSaved;

	boolean isPhotoCanceled;
	boolean isVideoCanceled;
	boolean isVoiceCanceled;
	boolean isHandwritingCanceled;

	Calendar mCalendar = Calendar.getInstance();
	TitleBitmapButton insertDateButton;

	int mSelectdContentArray;
	int mChoicedArrayItem;

	TitleBackgroundButton titleBackgroundBtn;
	TitleBitmapButton insertSaveBtn;
	TitleBitmapButton insertCancelBtn;
	TitleBitmapButton insert_textBtn;
	TitleBitmapButton insert_handwritingBtn;
	TitleBitmapButton deleteBtn;

	int textViewMode = 0;
	EditText insert_memoEdit;
	ImageView insert_handwriting;

	Animation translateLeftAnim;
	Animation translateRightAnim;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.memo_insert_activity);

		titleBackgroundBtn = (TitleBackgroundButton)findViewById(R.id.titleBackgroundBtn);
		mPhoto = (ImageView)findViewById(R.id.insert_photo);
    	mMemoEdit = (EditText) findViewById(R.id.insert_memoEdit);

    	insert_textBtn = (TitleBitmapButton)findViewById(R.id.insert_textBtn);
    	insert_handwritingBtn = (TitleBitmapButton)findViewById(R.id.insert_handwritingBtn);
    	insert_memoEdit = (EditText)findViewById(R.id.insert_memoEdit);
    	insert_handwriting = (ImageView)findViewById(R.id.insert_handwriting);
    	deleteBtn = (TitleBitmapButton)findViewById(R.id.deleteBtn);

    	translateLeftAnim = AnimationUtils.loadAnimation(this, R.anim.translate_left);
        translateRightAnim = AnimationUtils.loadAnimation(this, R.anim.translate_right);

        SlidingPageAnimationListener animListener = new SlidingPageAnimationListener();
        translateLeftAnim.setAnimationListener(animListener);
        translateRightAnim.setAnimationListener(animListener);

        insert_textBtn.setSelected(true);
        insert_handwritingBtn.setSelected(false);

    	insert_textBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (textViewMode == 1) {
					insert_handwriting.setVisibility(View.GONE);
					insert_memoEdit.setVisibility(View.VISIBLE);
					insert_memoEdit.startAnimation(translateLeftAnim);

					textViewMode = 0;
					insert_textBtn.setSelected(true);
					insert_handwritingBtn.setSelected(false);
				}
			}
		});

    	insert_handwritingBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (textViewMode == 0) {
					insert_handwriting.setVisibility(View.VISIBLE);
					insert_memoEdit.setVisibility(View.GONE);
					insert_handwriting.startAnimation(translateLeftAnim);

					textViewMode = 1;
					insert_handwritingBtn.setSelected(true);
					insert_textBtn.setSelected(false);
				}
			}
		});

    	mPhoto.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(isPhotoCaptured || isPhotoFileSaved) {
					showDialog(BasicInfo.CONTENT_PHOTO_EX);
				} else {
					showDialog(BasicInfo.CONTENT_PHOTO);
				}
			}
		});

    	insert_handwriting.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), HandwritingMakingActivity.class);
				startActivityForResult(intent, BasicInfo.REQ_HANDWRITING_MAKING_ACTIVITY);
			}
		});

    	deleteBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(BasicInfo.CONFIRM_DELETE);
			}
		});


		setBottomButtons();

		setMediaLayout();

		setCalendar();

		Intent intent = getIntent();
		mMemoMode = intent.getStringExtra(BasicInfo.KEY_MEMO_MODE);
		if(mMemoMode.equals(BasicInfo.MODE_MODIFY) || mMemoMode.equals(BasicInfo.MODE_VIEW)) {
			processIntent(intent);

			titleBackgroundBtn.setText("�޸� ����");
			insertSaveBtn.setText("����");

			deleteBtn.setVisibility(View.VISIBLE);
		} else {
			titleBackgroundBtn.setText("�� �޸�");
			insertSaveBtn.setText("����");

			deleteBtn.setVisibility(View.GONE);
		}
	}

    private class SlidingPageAnimationListener implements AnimationListener {

		public void onAnimationEnd(Animation animation) {

		}

		public void onAnimationRepeat(Animation animation) {

		}

		public void onAnimationStart(Animation animation) {

		}

    }



	public void processIntent(Intent intent) {
		mMemoId = intent.getStringExtra(BasicInfo.KEY_MEMO_ID);
		mMemoEdit.setText(intent.getStringExtra(BasicInfo.KEY_MEMO_TEXT));
		mMediaPhotoId = intent.getStringExtra(BasicInfo.KEY_ID_PHOTO);
		mMediaPhotoUri = intent.getStringExtra(BasicInfo.KEY_URI_PHOTO);
		mMediaVideoId = intent.getStringExtra(BasicInfo.KEY_ID_VIDEO);
		mMediaVideoUri = intent.getStringExtra(BasicInfo.KEY_URI_VIDEO);
		mMediaVoiceId = intent.getStringExtra(BasicInfo.KEY_ID_VOICE);
		mMediaVoiceUri = intent.getStringExtra(BasicInfo.KEY_URI_VOICE);
		mMediaHandwritingId = intent.getStringExtra(BasicInfo.KEY_ID_HANDWRITING);
		mMediaHandwritingUri = intent.getStringExtra(BasicInfo.KEY_URI_HANDWRITING);

		setMediaImage(mMediaPhotoId, mMediaPhotoUri, mMediaVideoId, mMediaVoiceId, mMediaHandwritingId);
    }


    public void setMediaImage(String photoId, String photoUri, String videoId, String voiceId, String handwritingId) {
    	Log.d(TAG, "photoId : " + photoId + ", photoUri : " + photoUri);

    	if(photoId.equals("") || photoId.equals("-1")) {
    		mPhoto.setImageResource(R.drawable.person_add);
    	} else {
    		isPhotoFileSaved = true;
    		mPhoto.setImageURI(Uri.parse(BasicInfo.FOLDER_PHOTO + photoUri));
    	}

    	if(handwritingId.equals("") || handwritingId.equals("-1")) {

    	} else {
    		isHandwritingFileSaved = true;
    		tempHandwritingUri = mMediaHandwritingUri;

    		Bitmap resultBitmap = BitmapFactory.decodeFile(BasicInfo.FOLDER_HANDWRITING + tempHandwritingUri);
    		insert_handwriting.setImageBitmap(resultBitmap);
    	}
    }


	/**
	 * �ϴ� �޴� ��ư ����
	 */
    public void setBottomButtons() {
    	insertSaveBtn = (TitleBitmapButton)findViewById(R.id.insert_saveBtn);
    	insertCancelBtn = (TitleBitmapButton)findViewById(R.id.insert_cancelBtn);

    	// ���� ��ư
    	insertSaveBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				boolean isParsed = parseValues();
                if (isParsed) {
                	if(mMemoMode.equals(BasicInfo.MODE_INSERT)) {
                		saveInput();
                	} else if(mMemoMode.equals(BasicInfo.MODE_MODIFY) || mMemoMode.equals(BasicInfo.MODE_VIEW)) {
                		modifyInput();
                	}
                }
			}
		});

    	// �ݱ� ��ư
    	insertCancelBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}


    /**
     * �����ͺ��̽��� ���ڵ� �߰�
     */
    private void saveInput() {

    	String photoFilename = insertPhoto();
    	int photoId = -1;

    	String SQL = null;

    	if (photoFilename != null) {
	    	// query picture id
	    	SQL = "select _ID from " + MemoDatabase.TABLE_PHOTO + " where URI = '" + photoFilename + "'";
	    	Log.d(TAG, "SQL : " + SQL);
	    	if (MultiMemoActivity.mDatabase != null) {
	    		Cursor cursor = MultiMemoActivity.mDatabase.rawQuery(SQL);
	    		if (cursor.moveToNext()) {
	    			photoId = cursor.getInt(0);
	    		}
	    		cursor.close();
	    	}
    	}


    	String handwritingFileName = insertHandwriting();
    	int handwritingId = -1;

    	if (handwritingFileName != null) {
	    	// query picture id
	    	SQL = "select _ID from " + MemoDatabase.TABLE_HANDWRITING + " where URI = '" + handwritingFileName + "'";
	    	Log.d(TAG, "SQL : " + SQL);
	    	if (MultiMemoActivity.mDatabase != null) {
	    		Cursor cursor = MultiMemoActivity.mDatabase.rawQuery(SQL);
	    		if (cursor.moveToNext()) {
	    			handwritingId = cursor.getInt(0);
	    		}
	    		cursor.close();
	    	}
    	}


    	SQL = "insert into " + MemoDatabase.TABLE_MEMO +
    				"(INPUT_DATE, CONTENT_TEXT, ID_PHOTO, ID_VIDEO, ID_VOICE, ID_HANDWRITING) values(" +
    				"DATETIME('" + mDateStr + "'), " +
    				"'"+ mMemoStr + "', " +
    				"'"+ photoId + "', " +
    				"'"+ "" + "', " +
    				"'"+ "" + "', " +
    				"'"+ handwritingId + "')";  		// Stage3 added

    	Log.d(TAG, "SQL : " + SQL);
    	if (MultiMemoActivity.mDatabase != null) {
    		MultiMemoActivity.mDatabase.execSQL(SQL);
    	}

    	Intent intent = getIntent();
    	setResult(RESULT_OK, intent);
    	finish();

    }

    /**
     * �����ͺ��̽� ���ڵ� ����
     */
    private void modifyInput() {

    	Intent intent = getIntent();

    	String photoFilename = insertPhoto();
    	int photoId = -1;

    	String SQL = null;

    	if (photoFilename != null) {
	    	// query picture id
	    	SQL = "select _ID from " + MemoDatabase.TABLE_PHOTO + " where URI = '" + photoFilename + "'";
	    	Log.d(TAG, "SQL : " + SQL);
	    	if (MultiMemoActivity.mDatabase != null) {
	    		Cursor cursor = MultiMemoActivity.mDatabase.rawQuery(SQL);
	    		if (cursor.moveToNext()) {
	    			photoId = cursor.getInt(0);
	    		}
	    		cursor.close();

	    		mMediaPhotoUri = photoFilename;

	    		SQL = "update " + MemoDatabase.TABLE_MEMO +
		    		" set " +
					" ID_PHOTO = '" + photoId + "'" +
					" where _id = '" + mMemoId + "'";

	    		if (MultiMemoActivity.mDatabase != null) {
	    			MultiMemoActivity.mDatabase.rawQuery(SQL);
	    		}

	    		mMediaPhotoId = String.valueOf(photoId);
	    	}
    	} else if(isPhotoCanceled && isPhotoFileSaved) {
    		SQL = "delete from " + MemoDatabase.TABLE_PHOTO +
    			" where _ID = '" + mMediaPhotoId + "'";
			Log.d(TAG, "SQL : " + SQL);
			if (MultiMemoActivity.mDatabase != null) {
				MultiMemoActivity.mDatabase.execSQL(SQL);
			}

			File photoFile = new File(BasicInfo.FOLDER_PHOTO + mMediaPhotoUri);
			if (photoFile.exists()) {
				photoFile.delete();
			}

			SQL = "update " + MemoDatabase.TABLE_MEMO +
    		" set " +
			" ID_PHOTO = '" + photoId + "'" +
			" where _id = '" + mMemoId + "'";

			if (MultiMemoActivity.mDatabase != null) {
				MultiMemoActivity.mDatabase.rawQuery(SQL);
			}

			mMediaPhotoId = String.valueOf(photoId);
    	}


    	String handwritingFileName = insertHandwriting();
    	int handwritingId = -1;

    	if (handwritingFileName != null) {
	    	// query picture id
	    	SQL = "select _ID from " + MemoDatabase.TABLE_HANDWRITING + " where URI = '" + handwritingFileName + "'";
	    	Log.d(TAG, "SQL : " + SQL);
	    	if (MultiMemoActivity.mDatabase != null) {
	    		Cursor cursor = MultiMemoActivity.mDatabase.rawQuery(SQL);
	    		if (cursor.moveToNext()) {
	    			handwritingId = cursor.getInt(0);
	    		}
	    		cursor.close();

	    		mMediaHandwritingUri = handwritingFileName;

	    		SQL = "update " + MemoDatabase.TABLE_MEMO +
	    			" set " +
	    			" ID_HANDWRITING = '" + handwritingId + "' " +
	    			" where _id = '" + mMemoId + "'";

	    		if (MultiMemoActivity.mDatabase != null) {
	    			MultiMemoActivity.mDatabase.rawQuery(SQL);
	    		}

	    		mMediaHandwritingId = String.valueOf(handwritingId);
	    	}
    	} else if(isHandwritingCanceled && isHandwritingFileSaved) {
			SQL = "delete from " + MemoDatabase.TABLE_HANDWRITING +
				" where _ID = '" + mMediaHandwritingId + "'";
			Log.d(TAG, "SQL : " + SQL);
			if (MultiMemoActivity.mDatabase != null) {
				MultiMemoActivity.mDatabase.execSQL(SQL);
			}

			File handwritingFile = new File(BasicInfo.FOLDER_HANDWRITING + mMediaHandwritingUri);
			if (handwritingFile.exists()) {
				handwritingFile.delete();
			}

			SQL = "update " + MemoDatabase.TABLE_MEMO +
			" set " +
			" ID_HANDWRITING = '" + handwritingId + "' " +
			" where _id = '" + mMemoId + "'";

			if (MultiMemoActivity.mDatabase != null) {
    			MultiMemoActivity.mDatabase.rawQuery(SQL);
    		}

			mMediaHandwritingId = String.valueOf(handwritingId);
    	}


    	// update memo info
    	SQL = "update " + MemoDatabase.TABLE_MEMO +
    				" set " +
    				" INPUT_DATE = DATETIME('" + mDateStr + "'), " +
    				" CONTENT_TEXT = '" + mMemoStr + "'" +
    				" where _id = '" + mMemoId + "'";

    	Log.d(TAG, "SQL : " + SQL);
    	if (MultiMemoActivity.mDatabase != null) {
    		MultiMemoActivity.mDatabase.execSQL(SQL);
    	}

    	intent.putExtra(BasicInfo.KEY_MEMO_TEXT, mMemoStr);
    	intent.putExtra(BasicInfo.KEY_ID_PHOTO, mMediaPhotoId);
    	intent.putExtra(BasicInfo.KEY_ID_VIDEO, mMediaVideoId);
    	intent.putExtra(BasicInfo.KEY_ID_VOICE, mMediaVoiceId);
    	intent.putExtra(BasicInfo.KEY_ID_HANDWRITING, mMediaHandwritingId);
    	intent.putExtra(BasicInfo.KEY_URI_PHOTO, mMediaPhotoUri);
    	intent.putExtra(BasicInfo.KEY_URI_VIDEO, mMediaVideoUri);
    	intent.putExtra(BasicInfo.KEY_URI_VOICE, mMediaVoiceUri);
    	intent.putExtra(BasicInfo.KEY_URI_HANDWRITING, mMediaHandwritingUri);

    	setResult(RESULT_OK, intent);
    	finish();
    }



    /**
     * �ٹ��� ������ ���� ������ ������ ��, PICTURE ���̺��� ���� ���� �߰�
     * �̹����� �̸��� ���� �ð��� �������� �� getTime() ���� ���ڿ� ���
     *
     * @return ���� �߰��� �̹����� �̸�
     */

    private String insertPhoto() {
       	String photoName = null;

    	if (isPhotoCaptured) { // captured Bitmap
	    	try {
	    		if (mMemoMode != null && mMemoMode.equals(BasicInfo.MODE_MODIFY)) {
	    			Log.d(TAG, "previous photo is newly created for modify mode.");

	    			String SQL = "delete from " + MemoDatabase.TABLE_PHOTO +
    				" where _ID = '" + mMediaPhotoId + "'";
			    	Log.d(TAG, "SQL : " + SQL);
			    	if (MultiMemoActivity.mDatabase != null) {
			    		MultiMemoActivity.mDatabase.execSQL(SQL);
			    	}

	    			File previousFile = new File(BasicInfo.FOLDER_PHOTO + mMediaPhotoUri);
	    	    	if (previousFile.exists()) {
	    	    		previousFile.delete();
	    	    	}
	    		}


	    		File photoFolder = new File(BasicInfo.FOLDER_PHOTO);

				//������ ���ٸ� ������ �����Ѵ�.
				if(!photoFolder.isDirectory()){
					Log.d(TAG, "creating photo folder : " + photoFolder);
					photoFolder.mkdirs();
				}

				// Temporary Hash for photo file name
				photoName = createFilename();

				FileOutputStream outstream = new FileOutputStream(BasicInfo.FOLDER_PHOTO + photoName);
				resultPhotoBitmap.compress(CompressFormat.PNG, 100, outstream);
				outstream.close();


				if (photoName != null) {
					Log.d(TAG, "isCaptured            : " +isPhotoCaptured);

			    	// INSERT PICTURE INFO
			    	String SQL = "insert into " + MemoDatabase.TABLE_PHOTO + "(URI) values(" + "'" + photoName + "')";
			    	if (MultiMemoActivity.mDatabase != null) {
			    		MultiMemoActivity.mDatabase.execSQL(SQL);
			    	}
				}

	    	} catch (IOException ex) {
	    		Log.d(TAG, "Exception in copying photo : " + ex.toString());
	    	}


    	}
    	return photoName;
    }


    private String insertHandwriting() {
       	String handwritingName = null;
    	Log.d(TAG, "isHandwritingMade            : " +isHandwritingMade);
    	if (isHandwritingMade) { // captured Bitmap
	    	try {

	    		if (mMemoMode != null && mMemoMode.equals(BasicInfo.MODE_MODIFY)) {
	    			Log.d(TAG, "previous handwriting is newly created for modify mode.");

	    			String SQL = "delete from " + MemoDatabase.TABLE_HANDWRITING +
    				" where _ID = '" + mMediaHandwritingId + "'";
			    	Log.d(TAG, "SQL : " + SQL);
			    	if (MultiMemoActivity.mDatabase != null) {
			    		MultiMemoActivity.mDatabase.execSQL(SQL);
			    	}

	    			File previousFile = new File(BasicInfo.FOLDER_HANDWRITING + mMediaHandwritingUri);
	    	    	if (previousFile.exists()) {
	    	    		previousFile.delete();
	    	    	}
	    		}


	    		File handwritingFolder = new File(BasicInfo.FOLDER_HANDWRITING);

				//������ ���ٸ� ������ �����Ѵ�.
				if(!handwritingFolder.isDirectory()){
					Log.d(TAG, "creating handwriting folder : " + handwritingFolder);
					handwritingFolder.mkdirs();
				}

				// Temporal Hash for handwriting file name

				handwritingName = createFilename();

				FileOutputStream outstream = new FileOutputStream(BasicInfo.FOLDER_HANDWRITING + handwritingName);
				// MIKE 20101215
				resultHandwritingBitmap.compress(CompressFormat.PNG, 100, outstream);
				// MIKE END
				outstream.close();


				if (handwritingName != null) {
					Log.d(TAG, "isCaptured            : " +isHandwritingMade);

			    	// INSERT HANDWRITING INFO
			    	String SQL = "insert into " + MemoDatabase.TABLE_HANDWRITING + "(URI) values(" + "'" + handwritingName + "')";
			    	if (MultiMemoActivity.mDatabase != null) {
			    		MultiMemoActivity.mDatabase.execSQL(SQL);
			    	}
				}

	    	} catch (IOException ex) {
	    		Log.d(TAG, "Exception in copying handwriting : " + ex.toString());
	    	}


    	}
    	return handwritingName;
    }



    private String createFilename() {
    	Date curDate = new Date();
    	String curDateStr = String.valueOf(curDate.getTime());

    	return curDateStr;
	}


    public void setMediaLayout() {
    	isPhotoCaptured = false;
    	isVideoRecorded = false;
    	isVoiceRecorded = false;
    	isHandwritingMade = false;

    	mVideoBtn = (TitleBitmapButton)findViewById(R.id.insert_videoBtn);
    	mVoiceBtn = (TitleBitmapButton)findViewById(R.id.insert_voiceBtn);

    }

    private void setCalendar(){
    	insertDateButton = (TitleBitmapButton) findViewById(R.id.insert_dateBtn);
    	insertDateButton.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			String mDateStr = insertDateButton.getText().toString();
    			Calendar calendar = Calendar.getInstance();
    			Date date = new Date();
    			try {
    				date = BasicInfo.dateDayNameFormat.parse(mDateStr);
    			} catch(Exception ex) {
    				Log.d(TAG, "Exception in parsing date : " + date);
    			}

    			calendar.setTime(date);

    			new DatePickerDialog(
    					MemoInsertActivity.this,
    					dateSetListener,
    					calendar.get(Calendar.YEAR),
    					calendar.get(Calendar.MONTH),
    					calendar.get(Calendar.DAY_OF_MONTH)
    					).show();

    		}
    	});

    	Date curDate = new Date();
    	mCalendar.setTime(curDate);

    	int year = mCalendar.get(Calendar.YEAR);
    	int monthOfYear = mCalendar.get(Calendar.MONTH);
    	int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);

    	insertDateButton.setText(year + "�� " + (monthOfYear+1) + "�� " + dayOfMonth + "��");

    }


    /**
     * ��¥ ���� ������
     */
    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			mCalendar.set(year, monthOfYear, dayOfMonth);
			insertDateButton.setText(year + "�� " + (monthOfYear+1) + "�� " + dayOfMonth + "��");
		}
	};


	/**
	 * ���ڿ� �޸� Ȯ��
	 */
    private boolean parseValues() {
    	String insertDateStr = insertDateButton.getText().toString();
    	try {
    		Date insertDate = BasicInfo.dateDayNameFormat.parse(insertDateStr);
    		mDateStr = BasicInfo.dateDayFormat.format(insertDate);
    	} catch(ParseException ex) {
    		Log.e(TAG, "Exception in parsing date : " + insertDateStr);
    	}

    	String memotxt = mMemoEdit.getText().toString();
    	mMemoStr = memotxt;

    	if (mMemoStr.trim().length() < 1) {
    		showDialog(BasicInfo.CONFIRM_TEXT_INPUT);
    		return false;
    	}

    	return true;
    }


	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = null;

		switch(id) {
			case BasicInfo.CONFIRM_TEXT_INPUT:
				builder = new AlertDialog.Builder(this);
				builder.setTitle("�޸�");
				builder.setMessage("�ؽ�Ʈ�� �Է��ϼ���.");
				builder.setPositiveButton("Ȯ��", new DialogInterface.OnClickListener() {
	        	    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

				break;

			case BasicInfo.CONTENT_PHOTO:
				builder = new AlertDialog.Builder(this);

				mSelectdContentArray = R.array.array_photo;
				builder.setTitle("�����ϼ���");
				builder.setSingleChoiceItems(mSelectdContentArray, 0, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	mChoicedArrayItem = whichButton;
	                }
	            });
				builder.setPositiveButton("����", new DialogInterface.OnClickListener() {
	        	    public void onClick(DialogInterface dialog, int whichButton) {
	        	    	if(mChoicedArrayItem == 0 ) {
	        	    		showPhotoCaptureActivity();
	        	    	} else if(mChoicedArrayItem == 1) {
	        	    		showPhotoLoadingActivity();
	        	    	}
	                }
	            });
				builder.setNegativeButton("���", new DialogInterface.OnClickListener() {
		             public void onClick(DialogInterface dialog, int whichButton) {

		            	 Log.d(TAG, "whichButton3        ======        " + whichButton);
	                 }
	            });

				break;

			case BasicInfo.CONTENT_PHOTO_EX:
				builder = new AlertDialog.Builder(this);

				mSelectdContentArray = R.array.array_photo_ex;
				builder.setTitle("�����ϼ���");
				builder.setSingleChoiceItems(mSelectdContentArray, 0, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	mChoicedArrayItem = whichButton;
	                }
	            });
				builder.setPositiveButton("����", new DialogInterface.OnClickListener() {
	        	    public void onClick(DialogInterface dialog, int whichButton) {
	        	    	if(mChoicedArrayItem == 0) {
	        	    		showPhotoCaptureActivity();
	        	    	} else if(mChoicedArrayItem == 1) {
	        	    		showPhotoLoadingActivity();
	        	    	} else if(mChoicedArrayItem == 2) {
	        	    		isPhotoCanceled = true;
	        	    		isPhotoCaptured = false;

	        	    		mPhoto.setImageResource(R.drawable.person_add);
	        	    	}
	                }
	            });
				builder.setNegativeButton("���", new DialogInterface.OnClickListener() {
		             public void onClick(DialogInterface dialog, int whichButton) {

	                 }
	             });

				break;

			case BasicInfo.CONFIRM_DELETE:
				builder = new AlertDialog.Builder(this);
				builder.setTitle("�޸�");
				builder.setMessage("�޸� �����Ͻðڽ��ϱ�?");
				builder.setPositiveButton("��", new DialogInterface.OnClickListener() {
	        	    public void onClick(DialogInterface dialog, int whichButton) {
	        	    	deleteMemo();
                    }
                });
				builder.setNegativeButton("�ƴϿ�", new DialogInterface.OnClickListener() {
		             public void onClick(DialogInterface dialog, int whichButton) {
		            	 dismissDialog(BasicInfo.CONFIRM_DELETE);
		             }
				});

				break;
			default:
				break;
		}

		return builder.create();
	}


    /**
     * �޸� ����
     */
    private void deleteMemo() {

    	// delete photo record
    	Log.d(TAG, "deleting previous photo record and file : " + mMediaPhotoId);
    	String SQL = "delete from " + MemoDatabase.TABLE_PHOTO +
    				" where _ID = '" + mMediaPhotoId + "'";
    	Log.d(TAG, "SQL : " + SQL);
    	if (MultiMemoActivity.mDatabase != null) {
    		MultiMemoActivity.mDatabase.execSQL(SQL);
    	}

    	File photoFile = new File(BasicInfo.FOLDER_PHOTO + mMediaPhotoUri);
    	if (photoFile.exists()) {
    		photoFile.delete();
    	}




    	// delete handwriting record
    	Log.d(TAG, "deleting previous handwriting record and file : " + mMediaHandwritingId);
    	SQL = "delete from " + MemoDatabase.TABLE_HANDWRITING +
    				" where _ID = '" + mMediaHandwritingId + "'";
    	Log.d(TAG, "SQL : " + SQL);
    	if (MultiMemoActivity.mDatabase != null) {
    		MultiMemoActivity.mDatabase.execSQL(SQL);
    	}

    	File handwritingFile = new File(BasicInfo.FOLDER_HANDWRITING + mMediaHandwritingUri);
    	if (handwritingFile.exists()) {
    		handwritingFile.delete();
    	}


    	// delete memo record
    	Log.d(TAG, "deleting previous memo record : " + mMemoId);
    	SQL = "delete from " + MemoDatabase.TABLE_MEMO +
    				" where _id = '" + mMemoId + "'";
    	Log.d(TAG, "SQL : " + SQL);
    	if (MultiMemoActivity.mDatabase != null) {
    		MultiMemoActivity.mDatabase.execSQL(SQL);
    	}

    	setResult(RESULT_OK);

		finish();
    }


	public void showPhotoCaptureActivity() {
		Intent intent = new Intent(getApplicationContext(), PhotoCaptureActivity.class);
		startActivityForResult(intent, BasicInfo.REQ_PHOTO_CAPTURE_ACTIVITY);
	}

	public void showPhotoLoadingActivity() {
		Intent intent = new Intent(getApplicationContext(), PhotoSelectionActivity.class);
		startActivityForResult(intent, BasicInfo.REQ_PHOTO_SELECTION_ACTIVITY);
	}

    /**
     * �ٸ� ��Ƽ��Ƽ�κ����� ���� ó��
     */
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		switch(requestCode) {
			case BasicInfo.REQ_PHOTO_CAPTURE_ACTIVITY:  // ���� ��� ���
				Log.d(TAG, "onActivityResult() for REQ_PHOTO_CAPTURE_ACTIVITY.");

				if (resultCode == RESULT_OK) {
					Log.d(TAG, "resultCode : " + resultCode);

					boolean isPhotoExists = checkCapturedPhotoFile();
			    	if (isPhotoExists) {
			    		Log.d(TAG, "image file exists : " + BasicInfo.FOLDER_PHOTO + "captured");

			    		resultPhotoBitmap = BitmapFactory.decodeFile(BasicInfo.FOLDER_PHOTO + "captured");

			    		tempPhotoUri = "captured";

			    		mPhoto.setImageBitmap(resultPhotoBitmap);
			            isPhotoCaptured = true;

			            mPhoto.invalidate();
			    	} else {
			    		Log.d(TAG, "image file doesn't exists : " + BasicInfo.FOLDER_PHOTO + "captured");
			    	}
				}

				break;

			case BasicInfo.REQ_PHOTO_SELECTION_ACTIVITY:  // ������ �ٹ����� �����ϴ� ���
				Log.d(TAG, "onActivityResult() for REQ_PHOTO_LOADING_ACTIVITY.");

				if (resultCode == RESULT_OK) {
					Log.d(TAG, "resultCode : " + resultCode);

					Uri getPhotoUri = intent.getParcelableExtra(BasicInfo.KEY_URI_PHOTO);
					try {
						BitmapFactory.Options options = new BitmapFactory.Options();
	    				options.inSampleSize = 4;

						resultPhotoBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(getPhotoUri), null, options);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}

					mPhoto.setImageBitmap(resultPhotoBitmap);
		            isPhotoCaptured = true;

		            mPhoto.invalidate();
				}

				break;


			case BasicInfo.REQ_HANDWRITING_MAKING_ACTIVITY:  // �ձ۾��� �����ϴ� ���
				Log.d(TAG, "onActivityResult() for REQ_HANDWRITING_MAKING_ACTIVITY.");

				if (resultCode == RESULT_OK) {
					boolean isHandwritingExists = checkMadeHandwritingFile();
					if(isHandwritingExists) {
						resultHandwritingBitmap = BitmapFactory.decodeFile(BasicInfo.FOLDER_HANDWRITING + "made");
						tempHandwritingUri = "made";

						isHandwritingMade = true;

			    		insert_handwriting.setImageBitmap(resultHandwritingBitmap);
					}
			    }

				break;

		}
	}


	/**
     * ����� ���� ���� Ȯ��
     */
    private boolean checkCapturedPhotoFile() {
    	File file = new File(BasicInfo.FOLDER_PHOTO + "captured");
    	if(file.exists()) {
    		return true;
    	}

    	return false;
    }


    /**
     * ����� �ձ۾� ���� Ȯ��
     */
    private boolean checkMadeHandwritingFile() {
    	File file = new File(BasicInfo.FOLDER_HANDWRITING + "made");
    	if(file.exists()) {
    		return true;
    	}

    	return false;
    }


}