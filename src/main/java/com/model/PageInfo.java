package com.model;

/**
 * Desc:
 * Author:Kevin
 * Date:2019/11/10
 **/
public class PageInfo {

    //总页码数据
    private int pagecount;
    //总条数
    private int count;
    //当前页码数
    private int currentpage;
    //每页条数
    private int pagesize;

    /**
     * @desc 返回分页信息
     * @Param: pagecount 总页码  count 总条数  currentpage 当前页码  pagesize 每页条数
     * @return PageInfo分页信息
    */
    public PageInfo(int pagecount, int count, int currentpage, int pagesize) {
        this.pagecount = pagecount;
        this.count = count;
        this.currentpage = currentpage;
        this.pagesize = pagesize;
    }

    public int getPagecount() {
        return pagecount;
    }

    public void setPagecount(int pagecount) {
        this.pagecount = pagecount;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCurrentpage() {
        return currentpage;
    }

    public void setCurrentpage(int currentpage) {
        this.currentpage = currentpage;
    }

    public int getPagesize() {
        return pagesize;
    }

    public void setPagesize(int pagesize) {
        this.pagesize = pagesize;
    }

    @Override
    public String toString() {
        return "PageInfo{" +
                "pagecount=" + pagecount +
                ", count=" + count +
                ", currentpage=" + currentpage +
                ", pagesize=" + pagesize +
                '}';
    }
}
