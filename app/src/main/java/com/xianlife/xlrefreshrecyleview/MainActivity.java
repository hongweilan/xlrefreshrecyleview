package com.xianlife.xlrefreshrecyleview;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Handler handler=new Handler();

    private XLRecyclerView xlRecyclerView;

    private MyAdapter myAdapter;

    List<String> list=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int i=0;i<60;i++){
            list.add("测试"+i);
        }

        xlRecyclerView=(XLRecyclerView)findViewById(R.id.refreshlayout);
        xlRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        xlRecyclerView.setMode(XLRecyclerView.Mode.Both);
        xlRecyclerView.setPreLoadEnble(true);
        myAdapter=new MyAdapter(this,list);
        myAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Object o) {
                Toast.makeText(MainActivity.this,"onItemClick",Toast.LENGTH_SHORT).show();
            }
        });
        myAdapter.setOnLongItemClickListener(new BaseAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(View view, Object o) {
                Toast.makeText(MainActivity.this,"onLongItemClick",Toast.LENGTH_SHORT).show();
            }
        });
        xlRecyclerView.setAdapter(myAdapter);

        xlRecyclerView.setOnRefreshListener(new XLRecyclerView.OnRefreshListener() {
            @Override
            public void onPullDownToRefresh(RecyclerView recyclerView) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        xlRecyclerView.onRefreshComplete("刷新完成",true);
                    }
                },5*1000);
            }

            @Override
            public void onPullUpToRefresh(RecyclerView recyclerView) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        xlRecyclerView.onRefreshComplete("加载完成",true);
                    }
                },5*1000);
            }

            @Override
            public void onScrollRefresh(RecyclerView recyclerView) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int size=list.size();
                        for (int i=size;i<size+60;i++){
                            list.add("测试"+i);
                        }
                        myAdapter.notifyDataSetChanged();
                        xlRecyclerView.onRefreshComplete("加载完成",true);
                    }
                },5*1000);
            }
        });

    }


    public void btnOnClick(View view){
        list.clear();
        myAdapter.notifyDataSetChangedForEmpty();
    }


}
