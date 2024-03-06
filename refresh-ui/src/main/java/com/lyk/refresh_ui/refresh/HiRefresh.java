package com.lyk.refresh_ui.refresh;

public interface HiRefresh {
    /**
     * 刷新时是否禁止滚动
     *
     * @param disableRefreshScroll 否禁止滚动
     */
    void setDisableRefreshScroll(boolean disableRefreshScroll);

    /**
     * 设置下拉刷新的监听器
     *
     * @param hiRefreshListener 刷新的监听器
     */
    void setRefreshListener(HiRefreshListener hiRefreshListener);

    void setRefreshTime(Long time);

    void setEnableRefresh(boolean enableRefresh);

    /**
     * 设置下拉刷新的视图
     *
     * @param hiOverView 下拉刷新的视图
     */
    void setRefreshOverView(HiOverView hiOverView);

    interface HiRefreshListener {

        void onRefreshStart();

        void onRefreshFinished();

    }
}
