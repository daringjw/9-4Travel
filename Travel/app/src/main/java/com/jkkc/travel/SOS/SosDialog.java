package com.jkkc.travel.SOS;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jkkc.travel.R;
import com.jkkc.travel.bean.Contact;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by Guan on 2017/6/8.
 */

public class SosDialog extends Dialog {

    Context context;
    private ListView mLvContacts;
    private List<Contact> mContent = new ArrayList<>();

    public SosDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.context = context;
    }

    public SosDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_sos);

        mLvContacts = (ListView) findViewById(R.id.lvContacts);
        initData();
        showData();


    }

    private void showData() {

        if (mContent != null) {

            mLvContacts.setAdapter(new ContactsAdapter(mContent));

            mLvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                    Log.e("onItemClick", "第" + position + "条目被点击了");

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_CALL);
                    //url:统一资源定位符
                    //uri:统一资源标示符（更广）
                    intent.setData(Uri.parse("tel:" + mContent.get(position).phone));
                    //开启系统拨号器
                    context.startActivity(intent);


                }
            });

        }


    }

    public static String getDeviceId(Context context) {
        TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        return TelephonyMgr.getDeviceId();
    }

    private static final String[] PHONES_PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.Contacts.Photo.PHOTO_ID,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID};

    /**
     * 联系人显示名称
     **/
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;

    /**
     * 电话号码
     **/
    private static final int PHONES_NUMBER_INDEX = 1;

    /**
     * 头像ID
     **/
    private static final int PHONES_PHOTO_ID_INDEX = 2;

    /**
     * 联系人的ID
     **/
    private static final int PHONES_CONTACT_ID_INDEX = 3;

    /**
     * 联系人名称
     **/
    private ArrayList<String> mContactsName = new ArrayList<String>();

    /**
     * 联系人头像
     **/
    private ArrayList<String> mContactsNumber = new ArrayList<String>();

    /**
     * 联系人头像
     **/
    private ArrayList<Bitmap> mContactsPhonto = new ArrayList<Bitmap>();

    /**
     * 得到手机通讯录联系人信息
     **/
    private List<Contact> getPhoneContacts() {

        ContentResolver resolver = context.getContentResolver();

        // 获取手机联系人
        Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PHONES_PROJECTION, null, null, null);

        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {

                //得到手机号码
                String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                //当手机号码为空的或者为空字段 跳过当前循环
                if (TextUtils.isEmpty(phoneNumber))
                    continue;

                //得到联系人名称
                String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);

                //得到联系人ID
                Long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);

                //得到联系人头像ID
                Long photoid = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);

                //得到联系人头像Bitamp
                Bitmap contactPhoto = null;

                //photoid 大于0 表示联系人有头像 如果没有给此人设置头像则给他一个默认的
                /*if (photoid > 0) {
                    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactid);
                    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);
                    contactPhoto = BitmapFactory.decodeStream(input);
                } else {
                    contactPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.contact_photo);
                }*/

                Contact contact = new Contact(contactName, phoneNumber);

                mContent.add(contact);

//                mContactsPhonto.add(contactPhoto);


            }

            phoneCursor.close();
        }

        return mContent;

    }

    private void initData() {

        /*Contact zhangsan = new Contact("张三", "999");
        Contact lisi = new Contact("李四", "110");
        Contact wangwu = new Contact("王五", "120");

        mContent.add(zhangsan);
        mContent.add(lisi);
        mContent.add(wangwu);*/

        mContent = getPhoneContacts();


    }


    class ContactsAdapter extends BaseAdapter {

        public ContactsAdapter(List<Contact> contacts) {

            mContent = contacts;


        }

        @Override
        public int getCount() {
            return mContent.size();
        }

        @Override
        public Object getItem(int position) {
            return mContent.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null) {

                convertView = View.inflate(getContext(), R.layout.item_contact, null);
                holder = new ViewHolder();
                holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
                holder.tvNumber = (TextView) convertView.findViewById(R.id.tvNumber);
                convertView.setTag(holder);

            } else {

                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvName.setText(mContent.get(position).name);
            holder.tvNumber.setText(mContent.get(position).phone);


            return convertView;
        }
    }


    static class ViewHolder {

        public TextView tvName;
        public TextView tvNumber;

    }

}
