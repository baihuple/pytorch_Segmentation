package com.example.myapplication.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Driver_pay_list;
import com.example.myapplication.HttpRequest;
import com.example.myapplication.InviteAdapter;
import com.example.myapplication.InviteList;
import com.example.myapplication.InviteProduct;
import com.example.myapplication.Message_list;
import com.example.myapplication.Msg;
import com.example.myapplication.R;
import com.example.myapplication.SuccessDialog;
import com.example.myapplication.application;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.FormBody;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Invite_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Invite_Fragment extends Fragment implements AdapterView.OnItemClickListener{

    final String TAG = "Baihupe";

    public String ip;
    public String url;

    private ListView listView;
    private BaseAdapter baseAdapter;
    private List<InviteProduct> list;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Invite_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Invite_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Invite_Fragment newInstance(String param1, String param2) {
        Invite_Fragment fragment = new Invite_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getInvitationList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_invite, container, false);               /** 重写 fragment,使用 root.findViewById **/

        // 4.构造 url
        application app = (application)getActivity().getApplication();
        ip =  app.getIp();
        url = "http://"+ip+"/repl/replInvite";


        // 如果中途有对补货商品的操作，那么重新存入 sharedPreference，操作完之后
        Button invite_BT = root.findViewById(R.id.invite_button);
        ImageButton delete_BT = root.findViewById(R.id.invite_delete_button);
        ImageView message_IV = root.findViewById(R.id.message_IV);
        ImageView invite_TV = root.findViewById(R.id.invite_list_IV);
        ImageView driver_list_IV = root.findViewById(R.id.driver_buy);

        // 点击跳转司机补货订单列表
        driver_list_IV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), Driver_pay_list.class);
                startActivity(intent);
            }
        });

        // 点击跳转邀约清单列表
        invite_TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), InviteList.class);
                startActivity(intent);
            }
        });

        // 点击跳转消息界面
        message_IV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), Message_list.class);
                startActivity(intent);
            }
        });

        // 渲染补货信息列表
        listView = root.findViewById(R.id.invite_LV);

        getInvitationList();

        // 邀约请求的点击事件
        invite_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // a、获取补货商品数据,从 SharedPreference 中
                SharedPreferences record = getActivity().getSharedPreferences("Info_Record",MODE_PRIVATE);
                String invitation_Info = record.getString("invitation","no_Info");                      // 所有内容清空后，整个 invitation 清空
                //Log.i(TAG,"获取邀请数据"+invitation_Info);
                // 邀请列表为空，发出提示
                if(invitation_Info.equals("no_Info")){                  // 缓存为空
                    myDialog_Warning("您的邀请列表为空，请添加商品后再发送邀请");
                }else{                              // 不为空，则获取 JSONARRAY 数组，发送请求

                    HttpRequest httpRequest = new HttpRequest();

                    // 1. 获取头部所需数据
                    String token = record.getString("token","token 值为空");
                    String identity = "1";

                    // 2. 获取发送的数据
                    // 2.1 从 stationInfo 中获取 city
                    String city = "";
                    String stationaddr = "";
                    try {
                        JSONObject stationInfo = new JSONObject(record.getString("stationInfo","站点信息不存在"));
                         city = stationInfo.getString("city");
                        // 获取补货站站点位置
                         stationaddr = stationInfo.getString("stationaddr");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    // 2.2 构造 invite_message, invitation 这个 JSONArray 外层再构造 JSONArray
                    String message = record.getString("invitation","获取邀约信息失败");

                    // 3. 构造 header
                    Map<String,String> headers = new TreeMap<>();
                    headers.put("Authorization",token);
                    headers.put("identity","1");

                    // 4. 构造 formbody
                    FormBody formBody = new FormBody.Builder().add("city",city).add("message",message).add("stationaddr",stationaddr).build();



                    // 5、发送请求
                    String result = "";
                    try {
                        result = httpRequest.OkHttpsPost_F(url,formBody,headers);
                        // 6、验证邀约是否成功
                        JSONObject back = new JSONObject(result);
                        if(back.getString("code").equals("200")){

                            // 清除 invitation 缓存,同时保存缓存到另一个已邀约 invited_list
                            SharedPreferences.Editor edit = record.edit();

                            // 如果缓存为空，直接放入；否则取出在放入
                            String invited_list = record.getString("invited_list","no_invited_list");
                            if(invited_list.equals("no_invited_list")){     // 如果邀约为空
                                JSONArray jsonArray = new JSONArray();
                                JSONObject jsonObject = new JSONObject();

                                Msg msg = new Msg();    // 该声明，只是为了获取当前时间；其他没有实际意义
                                jsonObject.put("time",msg.getNow_time());        // 放入时间
                                jsonObject.put("content",message);
                                jsonArray.put(jsonObject);

                                edit.putString("invited_list",jsonArray.toString());
                                edit.commit();
                            }else{  // 邀约不为空
                                JSONArray jsonArray = new JSONArray(invited_list);

                                JSONObject jsonObject = new JSONObject();
                                Msg msg = new Msg();    // 该声明，只是为了获取当前时间；其他没有实际意义
                                jsonObject.put("time",msg.getNow_time());        // 放入时间
                                jsonObject.put("content",message);
                                jsonArray.put(jsonObject);

                                edit.putString("invited_list",jsonArray.toString());
                                edit.commit();
                            }

                            // 清除邀约清单的缓存
                            edit.remove("invitation");
                            edit.commit();
                            getInvitationList();
                            myDialog_Successful_Own("邀约发布成功");

                        }else{
                            myDialog_Warning("邀约请求发送失败");
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        // 清空邀约信息的点击事件
        delete_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences record = getActivity().getSharedPreferences("Info_Record",MODE_PRIVATE);
                String invitation = record.getString("invitation","stationInfo_null");
                Log.i(TAG,invitation);

                // 如果为空，发出警告
                if(invitation.equals("stationInfo_null")){
                    myDialog_Warning("邀请信息为空，请先选择商品");
                }else{
                    // 否则清空信息
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("提示");
                    builder.setMessage("您确定要将邀请列表清空吗？");
                    builder.setIcon(R.drawable.notice);
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences.Editor edit = record.edit();
                            edit.remove("invitation");
                            edit.commit();

                            getInvitationList();
                            /** 清空完之后，订单信息也为空！！！！！！！！！！！！！！！ **/

                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                }

            }
        });

        return root;

    }

    public void myDialog_Warning(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("注意");
        builder.setMessage(message);
        builder.setIcon(R.drawable.attention);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void myDialog_Successful(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("提示");
        builder.setMessage(message);
        builder.setIcon(R.drawable.successful);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void myDialog_Successful_Own(String message){
        SuccessDialog.Builder builder = new SuccessDialog.Builder(getContext());
        builder.setMessage(message);
        builder.setButton("确认",view -> {
        });
        SuccessDialog successDialog = builder.create();
        successDialog.show();

    }

    /** 点击列表项响应事件 **/
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(getContext(),"你点击了sadf"+i,Toast.LENGTH_SHORT).show();
    }

    /** 点击删除按钮响应事件 **/
    private InviteAdapter.MyClickListener mListener = new InviteAdapter.MyClickListener(){
        @Override
        public void myOnClick(int position, View v) throws JSONException {
            InviteProduct product = list.get(position);
            String storeid = product.getStoreid();
            // Log.i(TAG,storeid);
            // 内存获取 invitation
            SharedPreferences record = getActivity().getSharedPreferences("Info_Record",MODE_PRIVATE);
            String inv = record.getString("invitation","no_invitation");
            JSONArray invJSONArray = new JSONArray(inv);

            for(int i=0;i<invJSONArray.length();i++){
                JSONObject invJSONObject = invJSONArray.getJSONObject(i);
                if(invJSONObject.getString("storeid").equals(storeid)){
                    invJSONArray.remove(i);
                    break;
                }
            }

            // 重新提交回缓存中
            SharedPreferences.Editor edit = record.edit();
            edit.putString("invitation", invJSONArray.toString());   // 存入缓存
            edit.commit();

            getInvitationList();

            myDialog_Successful("删除成功");

        }
    };


    /** 获取邀请列表并且渲染 **/
    private void getInvitationList(){
        list = new ArrayList<>();
        // 从 sharedPreferences 中获取保存的信息
        SharedPreferences record = getActivity().getSharedPreferences("Info_Record", MODE_PRIVATE);
        String invite_sto = record.getString("invitation","no_Info");
        // 不存在，则不渲染
        if(invite_sto.equals("no_Info")){
            // 也渲染
            // 存在则获取信息 JSONArray 并且渲染
            baseAdapter = new InviteAdapter(getContext(),list,mListener);
            listView.setAdapter(baseAdapter);
            listView.setOnItemClickListener(this);
            baseAdapter.notifyDataSetChanged();                     // 列表数据发生变化,更新列表数据
        }else{
            // 存在则获取信息 JSONArray 并且渲染
            try {
                JSONArray proInvitearray = new JSONArray(invite_sto);
                for(int i=0;i<proInvitearray.length();i++){
                    JSONObject proInvite = proInvitearray.getJSONObject(i);
                    // 用 InviteProduct 对象进行保存
                    InviteProduct inviteProduct = new InviteProduct();
                    inviteProduct.setInvite_name(proInvite.getString("comName"));
                    inviteProduct.setInvite_price(proInvite.getString("price"));
                    inviteProduct.setInvite_num("x "+proInvite.getString("number"));
                    inviteProduct.setStoreid(proInvite.getString("storeid"));
                    list.add(inviteProduct);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            baseAdapter = new InviteAdapter(getContext(),list,mListener);
            listView.setAdapter(baseAdapter);
            listView.setOnItemClickListener(this);
            baseAdapter.notifyDataSetChanged();                     // 列表数据发生变化,更新列表数据
        }
    }


}