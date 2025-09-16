package com.example.demo.dto;

import com.example.demo.model.User;
import java.util.List;

public class AdvancedSearchResponse {
    private List<User> users;
    private long totalCount;
    private int page;
    private int size;

    public AdvancedSearchResponse(List<User> users, long totalCount, int page, int size) {
        this.users = users;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
