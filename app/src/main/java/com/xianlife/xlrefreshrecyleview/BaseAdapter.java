package com.xianlife.xlrefreshrecyleview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by LanHongwei on 2017/4/1.
 */

public abstract class BaseAdapter extends RecyclerView.Adapter implements View.OnClickListener{

    public Context context;

    private View headerView;
    private View footerView;
    private int headContentHeight=0;
    private int footContentHeight=0;

    private View emptyView;
    private XLRecyclerView xlRecyclerView;

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public BaseAdapter(Context context){
        this.context=context;
    }

    @Override
    public final int getItemViewType(int position) {
        if(position==0){
            return ViewType.viewType1;
        }else if(position==getItemCount()-1){
            return ViewType.viewType2;
        }else {
            return getViewType(position-1);
        }
    }

    /**
     * 返回Item类型(不能为-1或-2)
     * @param position
     * @return
     *       不能为-1或-2
     */
    public abstract int getViewType(int position);

    @Override
    public final int getItemCount() {
        return getCount()+2;
    }

    /**
     * 返回数据数量
     * @return
     */
    public abstract int getCount();

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType== ViewType.viewType1){
            headerView=LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header,null,true);
            measureView(headerView);
            headContentHeight = headerView.getMeasuredHeight();
            headerView.setPadding(0, -1 * headContentHeight, 0, 0);
            return new HeaderViewHolder(headerView);
        }else if(viewType== ViewType.viewType2){
            footerView=LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_footer,null,true);
            measureView(footerView);
            footContentHeight = footerView.getMeasuredHeight()-1;
            footerView.setPadding(0, 0, 0, -1 * footContentHeight+1);
            return new FooterViewHolder(footerView);
        }else{
            RecyclerView.ViewHolder viewHolder=createContentViewHolder(parent,viewType);
            viewHolder.itemView.setOnClickListener(this);
            return viewHolder;
        }
    }

    public abstract RecyclerView.ViewHolder createContentViewHolder(ViewGroup parent, int viewType);

    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType=getItemViewType(position);
        if(viewType== ViewType.viewType1){
            //nothing
        }else if (viewType== ViewType.viewType2){
            //nothing
        }else{
            bindContentViewHolder(holder,position-1);
            holder.itemView.setTag(setTagForItem(position-1));
        }
    }

    public abstract void bindContentViewHolder(RecyclerView.ViewHolder holder, int position);

    public abstract Object setTagForItem(int position);

    /**
     * 数据源变化后刷新界面（如果数据源为空，显示EmptyView）
     */
    public final void notifyDataSetChangedForEmpty() {
        notifyDataSetChanged();
        emptyObserver.onChanged();
    }

    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener l){
        mOnItemClickListener = l;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v,v.getTag());
        }
    }

    private final class HeaderViewHolder extends RecyclerView.ViewHolder{

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    private final class FooterViewHolder extends RecyclerView.ViewHolder{

        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    public View getHeaderView(){
        return headerView;
    }

    public int getHeadContentHeight(){
        return headContentHeight;
    }

    public View getFooterView(){
        return footerView;
    }

    public int getFootContentHeight(){
        return footContentHeight;
    }

    public void setEmptyView(View emptyView){
        this.emptyView=emptyView;
    }

    public void setXLRecyclerView(XLRecyclerView xlRecyclerView){
        this.xlRecyclerView=xlRecyclerView;
    }

    public RecyclerView.AdapterDataObserver getEmptyObserver(){
        return emptyObserver;
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight, View.MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    private RecyclerView.AdapterDataObserver emptyObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            if (emptyView != null&&xlRecyclerView!=null) {
                if (getItemCount() <= 2) {
                    emptyView.setVisibility(View.VISIBLE);
                    xlRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    xlRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    public  interface OnRecyclerViewItemClickListener {
        void onItemClick(View view , Object o);
    }

    private final class ViewType{
        public static final int viewType1=-1;
        public static final int viewType2=-2;
    }

}
