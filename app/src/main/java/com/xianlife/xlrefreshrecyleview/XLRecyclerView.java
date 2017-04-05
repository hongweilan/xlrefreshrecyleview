package com.xianlife.xlrefreshrecyleview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;

/**
 * Created by LanHongwei on 2017/3/31.
 */

public class XLRecyclerView extends RecyclerView {

    private final static int PULL_To_REFRESH = 0;
    private final static int RELEASE_To_REFRESH = 1;
    private final static int REFRESHING = 2;
    private final static int DONE = 3;
    public enum Mode{
        Start ,
        End,
        Disable,
        Both;
    }


    private View headerView;
    private TextView title,subTitle;
    private View footerView;
    private TextView footerTitle;

    private SharedPreferences sp;
    private int headContentHeight=0;
    private int footContentHeight=0;

    private float preY=0,downY=0;
    private float curX=0,curY=0;
    private int touchSlop=0;//触发移动事件的最短距离
    private boolean preLoading=false;
    private boolean downLoading=false;
    private boolean upLoading =false;
    private boolean hasMore=false;
    private boolean preLoadEnble=false;

    private int state=DONE;
    private Mode mode= Mode.Disable;

    private OnRefreshListener mOnRefreshListener;

    public XLRecyclerView(Context context) {
        super(context);
        init();
    }

    public XLRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public XLRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        touchSlop = configuration.getTouchSlop();
        sp=getContext().getSharedPreferences("xl_recyclerview" , Context.MODE_PRIVATE);
        this.addOnScrollListener(new RecyclerViewOnScrollListener());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                curX = ev.getRawX();
                preY = curY = downY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float x=ev.getRawX();
                float y=ev.getRawY();
                if(Math.abs(y-curY)>Math.abs(x-curX)){
                    if(Math.abs(y-downY)>touchSlop){
                        curX = x;
                        curY = y;
                        return true;
                    }
                }
                curX = x;
                curY = y;
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                float y=ev.getRawY();
                if(mode== Mode.Start||mode== Mode.Both){
                    if(canDownPull()){
                        changeHeaderViewByMove((int) (y - preY)/2);
                    }
                }
                if(mode== Mode.End||mode== Mode.Both){
                    if(canUpPull()){
                       changeFooterViewByMove((int) (preY - y)/2);
                    }
                }
                preY=y;
                break;
            case MotionEvent.ACTION_UP:
                if(canDownPull()){
                    if(mode== Mode.Start||mode== Mode.Both){
                        changeHeaderViewByUp();
                    }
                }
                if(canUpPull()){
                    if(mode== Mode.End||mode== Mode.Both){
                        changeFooterViewByUp();
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if(canDownPull()){
                    if(mode== Mode.Start||mode== Mode.Both){
                        changeHeaderViewByUp();
                    }
                }
                if(canUpPull()){
                    if(mode== Mode.End||mode== Mode.Both){
                        changeFooterViewByUp();
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private boolean canDownPull(){
        if(getLayoutManager() instanceof LinearLayoutManager){
            int firstVisibleItem=((LinearLayoutManager)getLayoutManager()).findFirstVisibleItemPosition();
            if((firstVisibleItem==1||firstVisibleItem==0)&&headerView.getPaddingTop()>=-headContentHeight){
                return true;
            }else{
                headerView.setPadding(0, -headContentHeight, 0, 0);
                return false;
            }
        }
        return false;
    }

    private boolean canUpPull(){
        if(getLayoutManager() instanceof LinearLayoutManager){
            int lastVisibleItem=((LinearLayoutManager)getLayoutManager()).findLastVisibleItemPosition();
            int itemCount=getAdapter().getItemCount();
            if(lastVisibleItem>0&&(lastVisibleItem==itemCount-1||lastVisibleItem==itemCount-2)){
                return true;
            }else{
                return false;
            }
        }
        return false;
    }

    private void changeHeaderViewByMove(int value){
        if(!downLoading){
            if(state==DONE){
                setState(PULL_To_REFRESH);
                headerView.setPadding(0, -headContentHeight, 0, 0);
            }else if(state==PULL_To_REFRESH){
                if(headerView.getPaddingTop()>=0){
                    setState(RELEASE_To_REFRESH);
                    if(preLoading){
                        changeHeadherViewLable("正在预加载数据...",true,false);
                    }else {
                        changeHeadherViewLable("松开立即刷新...",true,false);
                    }
                }else{
                    if(preLoading){
                        changeHeadherViewLable("正在预加载数据...",true,false);
                    }else {
                        changeHeadherViewLable("下拉刷新...",true,false);
                    }
                }
                headerView.setPadding(0, value + headerView.getPaddingTop(), 0, 0);
            }else if(state==RELEASE_To_REFRESH){
                if(headerView.getPaddingTop()<0){
                    setState(PULL_To_REFRESH);
                }
                headerView.setPadding(0, value + headerView.getPaddingTop(), 0, 0);
            }
        }else{
            if(headerView.getPaddingTop()>=0){
                headerView.setPadding(0, value + headerView.getPaddingTop(), 0, 0);
            }else {
                headerView.setPadding(0, 0, 0, 0);
            }
        }

    }

    private void changeHeaderViewByUp(){
        if(!downLoading){
            if(state==PULL_To_REFRESH){
                setState(DONE);
                animateHeaderView(-headContentHeight,false);
            }else if(state==RELEASE_To_REFRESH){
                if(preLoading){
                    setState(DONE);
                    animateHeaderView(-headContentHeight,false);
                }else {
                    setState(REFRESHING);
                    animateHeaderView(0,true);
                    downLoading =true;
                }
            }
        }else{
            if(headerView.getPaddingTop()>=0){
                animateHeaderView(0,false);
            }else {
                headerView.setPadding(0, 0, 0, 0);
            }
        }
    }

    private void changeHeadherViewLable(String titleText,boolean showSubTitle,boolean saveTime){
        title.setText(titleText);
        if(showSubTitle){
            subTitle.setVisibility(VISIBLE);
        }else {
            subTitle.setVisibility(GONE);
        }
        long updateTime=sp.getLong("update_time",0);
        if(updateTime==0){
            subTitle.setText("首次下拉刷新");
        }else{
            SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
            String ctime = formatter.format(updateTime);
            subTitle.setText("上次更新于:"+ctime);
        }
        if(saveTime){
            SharedPreferences.Editor editor=sp.edit();
            editor.putLong("update_time",System.currentTimeMillis());
            editor.commit();
        }
    }

    private void changeFooterViewByMove(int value){
        if(footerView==null){
            return;
        }
        if(!downLoading){
            if(!upLoading){
                if(state==DONE){
                    setState(PULL_To_REFRESH);
                    footerView.setPadding(0, 0, 0, -footContentHeight);
                }else if(state==PULL_To_REFRESH){
                    if(footerView.getPaddingBottom()>=0){
                        setState(RELEASE_To_REFRESH);
                        changeFooterViewLable("松开立即刷新...");
                    }else{
                        changeFooterViewLable("上拉刷新...");
                    }
                    footerView.setPadding(0, 0, 0, value + footerView.getPaddingBottom());
                }else if(state==RELEASE_To_REFRESH){
                    if(footerView.getPaddingBottom()<0){
                        setState(PULL_To_REFRESH);
                    }
                    footerView.setPadding(0, 0, 0, value + footerView.getPaddingBottom());
                }
            }else{
                footerView.setPadding(0, 0, 0, value + footerView.getPaddingBottom());
            }
        }
    }

    private void changeFooterViewByUp(){
        if(footerView==null){
            return;
        }
        if(!downLoading){
            if(!upLoading){
                if(state==PULL_To_REFRESH){
                    setState(DONE);
                    animateFooterView(-footContentHeight,false);
                }else if(state==RELEASE_To_REFRESH){
                    setState(REFRESHING);
                    animateFooterView(0,true);
                    upLoading =true;
                }
            }else{
                if(footerView.getPaddingBottom()>0){
                    animateFooterView(0,false);
                }
            }
        }
    }

    private void changeFooterViewLable(String footerTitleText){
        footerTitle.setText(footerTitleText);
    }

    public void onRefreshComplete(String refreshResult,boolean hasMore){
        this.hasMore=hasMore;
        if(downLoading){
            downLoading =false;
            setState(DONE);
            changeHeadherViewLable(refreshResult,false,true);
            animateHeaderView(-headContentHeight,false);
        }
        if(upLoading){
            upLoading=false;
            setState(DONE);
            changeFooterViewLable(refreshResult);
            animateFooterView(-footContentHeight,false);
        }
        if(preLoading){
            preLoading=false;
        }
        if(!hasMore){
            if(mode== Mode.Both){
                setMode(Mode.Start);
            }else if(mode== Mode.End){
                setMode(Mode.Disable);
            }
        }
    }

    private void animateHeaderView(int end,boolean isListener){
        ObjectAnimator objectAnimator= ObjectAnimator.ofInt(new HeaderViewWrapper(headerView), "paddingTop", headerView.getPaddingTop(), end).setDuration(300);
        if(isListener){
            objectAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    changeHeadherViewLable("正在刷新...",false,false);
                    if(mOnRefreshListener!=null){
                        mOnRefreshListener.onPullDownToRefresh(XLRecyclerView.this);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }
        objectAnimator.start();
    }

    private void animateFooterView(int end,boolean isListener){
        if(footerView==null){
            return;
        }
        ObjectAnimator objectAnimator=ObjectAnimator.ofInt(new FooterViewWrapper(footerView), "paddingBottom", footerView.getPaddingBottom(), end).setDuration(300);
        if(isListener){
            objectAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    changeFooterViewLable("正在刷新...");
                    if(mOnRefreshListener!=null){
                        if(!preLoading){
                            mOnRefreshListener.onPullUpToRefresh(XLRecyclerView.this);
                        }
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }
        objectAnimator.start();
    }

    public void setMode(Mode mode){
        this.mode=mode;
    }

    private void setState(int state){
        this.state=state;
    }

    public void setPreLoadEnble(boolean preLoadEnble){
        this.hasMore=true;
        this.preLoadEnble=preLoadEnble;
    }

    /**
     * 必须在setAdapter之后调用
     * @param emptyViewRes
     */
    public void setEmptyView(int emptyViewRes){
        if(getAdapter() instanceof BaseAdapter){
            View emptyView=initEmptyView(emptyViewRes);
            ((BaseAdapter)getAdapter()).setEmptyView(emptyView);
            ((BaseAdapter)getAdapter()).setXLRecyclerView(this);
             getAdapter().registerAdapterDataObserver(((BaseAdapter)getAdapter()).getEmptyObserver());
            ((BaseAdapter)getAdapter()).getEmptyObserver().onChanged();
        }else{
            Log.e("setEmptyView ","setEmptyView方法必须在setAdapter方法之后调用 ");
        }
    }

    private View initEmptyView(int emptyViewRes) {
        View mEmptyView = LayoutInflater.from(getContext()).inflate(emptyViewRes,null,true);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ((ViewGroup)this.getParent()).addView(mEmptyView,lp);
        return mEmptyView;
    }

    public void setOnRefreshListener(OnRefreshListener l){
        this.mOnRefreshListener=l;
    }

    private class HeaderViewWrapper {

        private View mTargetView;

        public HeaderViewWrapper(View target) {
            mTargetView = target;
        }

        public int getPaddingTop() {
            return mTargetView.getPaddingTop();
        }
        public void setPaddingTop(int paddingTop) {
            mTargetView.setPadding(0,paddingTop,0,0);
            mTargetView.requestLayout();
        }
    }

    private class FooterViewWrapper {

        private View mTargetView;

        public FooterViewWrapper(View target) {
            mTargetView = target;
        }

        public int getPaddingBottom() {
            return mTargetView.getPaddingBottom();
        }
        public void setPaddingBottom(int paddingBottom) {
            mTargetView.setPadding(0,0,0,paddingBottom);
            mTargetView.requestLayout();
        }
    }

    public interface OnRefreshListener{
        public abstract void onPullDownToRefresh(RecyclerView recyclerView);
        public abstract void onPullUpToRefresh(RecyclerView recyclerView);
        public abstract void onScrollRefresh(RecyclerView recyclerView);
    }

    class RecyclerViewOnScrollListener extends OnScrollListener{

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if(getLayoutManager() instanceof LinearLayoutManager){
                if(headerView==null){
                    int firstVisibleItem=((LinearLayoutManager)getLayoutManager()).findFirstVisibleItemPosition();
                    if(firstVisibleItem==0||firstVisibleItem==1){
                        if(getAdapter() instanceof BaseAdapter){
                            headerView=((BaseAdapter)getAdapter()).getHeaderView();
                            headContentHeight=((BaseAdapter)getAdapter()).getHeadContentHeight();
                            title=(TextView)headerView.findViewById(R.id.pull_to_refresh_title);
                            subTitle=(TextView)headerView.findViewById(R.id.pull_to_refresh_subtitle);
                        }
                    }
                }
                int lastVisibleItem=((LinearLayoutManager)getLayoutManager()).findLastVisibleItemPosition();
                int itemCount=getAdapter().getItemCount();
                if(footerView==null){
                    if(lastVisibleItem==itemCount-1){
                        if(getAdapter() instanceof BaseAdapter){
                            footerView=((BaseAdapter)getAdapter()).getFooterView();
                            footContentHeight=((BaseAdapter)getAdapter()).getFootContentHeight();
                            footerTitle=(TextView)footerView.findViewById(R.id.pull_to_refresh_footertitle);
                        }
                    }
                }
                if(preLoadEnble&&footerView!=null){
                    boolean canPreLoad=itemCount>3&&lastVisibleItem>=itemCount-3;
                    if(!preLoading&&!downLoading&&!upLoading&&canPreLoad&&hasMore){
                        preLoading=true;
                        setState(DONE);
                        footerView.setPadding(0, 0, 0, 0);
                        upLoading =true;
                        changeFooterViewLable("正在刷新...");
                        if(mOnRefreshListener!=null){
                            mOnRefreshListener.onScrollRefresh(XLRecyclerView.this);
                        }
                    }
                }
            }
        }
    }


}
