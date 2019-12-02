package com.zzming.xsapi.util.excel;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class SheetBean {
    private LinkedHashMap<String, String> products;
    private LinkedHashMap<String, String> modules;
    private LinkedHashSet<String> functionSet;
    private LinkedHashMap<String, String> aboutMap;
    private LinkedHashMap<String, String> stepMap;
    private LinkedHashMap<String, String> resultMap;
    private LinkedHashMap<String, String> type;
    private LinkedHashMap<String, String> useType;
    private LinkedHashMap<String, String> priority;
    private String sheetName;

    public String getSheetName() {
        return this.sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public LinkedHashMap<String, String> getType() {
        return this.type;
    }

    public void setType(LinkedHashMap<String, String> type) {
        this.type = type;
    }

    public LinkedHashMap<String, String> getUseType() {
        return this.useType;
    }

    public void setUseType(LinkedHashMap<String, String> useType) {
        this.useType = useType;
    }

    public LinkedHashMap<String, String> getProducts() {
        return this.products;
    }

    public void setProducts(LinkedHashMap<String, String> products) {
        this.products = products;
    }

    public LinkedHashMap<String, String> getModules() {
        return this.modules;
    }

    public void setModules(LinkedHashMap<String, String> modules) {
        this.modules = modules;
    }

    public LinkedHashSet<String> getFunctionSet() {
        return this.functionSet;
    }

    public void setFunctionSet(LinkedHashSet<String> functionSet) {
        this.functionSet = functionSet;
    }

    public LinkedHashMap<String, String> getAboutMap() {
        return this.aboutMap;
    }

    public void setAboutMap(LinkedHashMap<String, String> aboutMap) {
        this.aboutMap = aboutMap;
    }

    public LinkedHashMap<String, String> getStepMap() {
        return this.stepMap;
    }

    public void setStepMap(LinkedHashMap<String, String> stepMap) {
        this.stepMap = stepMap;
    }

    public LinkedHashMap<String, String> getResultMap() {
        return this.resultMap;
    }

    public void setResultMap(LinkedHashMap<String, String> resultMap) {
        this.resultMap = resultMap;
    }

    public LinkedHashMap<String, String> getPriority() {
        return this.priority;
    }

    public void setPriority(LinkedHashMap<String, String> priority) {
        this.priority = priority;
    }
}