package com.xianlife.xlrefreshrecyleview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by LanHongwei on 2017/3/29.
 */

public class MyAdapter extends BaseAdapter{

    List<String> list;

    public MyAdapter(Context context,List<String> list){
        super(context);
        this.list=list;
    }

    @Override
    public int getViewType(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public RecyclerView.ViewHolder createContentViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(new TextView(context));
    }

    @Override
    public void bindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MyViewHolder)holder).textView.setText(list.get(position)+"   "+position);
    }

    @Override
    public Object setTagForItem(int position) {
        return list.get(position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView textView;

        public MyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }

}
