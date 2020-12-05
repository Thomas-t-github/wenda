package com.nowcoder.service;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class SensitiveService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveService.class);

    /**
     * 敏感词替换符
     */
    private static final String DEFAULT_REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    @Override
    public void afterPropertiesSet() throws Exception {

        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader reader = null;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream("SensitiveWords.txt");
            isr = new InputStreamReader(is);
            reader = new BufferedReader(isr);
            String lineTxt;
            while ((lineTxt = reader.readLine()) != null){
                lineTxt = lineTxt.trim();
                addWord(lineTxt);
            }

        } catch (Exception e) {
            logger.error("读取敏感词文件失败："+e.getMessage());
        } finally {
            try {
                if ( reader != null){
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if ( isr != null){
                    isr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if ( is != null){
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * 过滤关键词
     * @param text
     * @return
     */
    public String filter(String text){

        StringBuilder result = new StringBuilder();
        String replacement = DEFAULT_REPLACEMENT;

        if (StringUtils.isBlank(text)){
            return text;
        }

        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;

        while (begin < text.length()){
            char c = text.charAt(position);
            //符号直接跳过
            if (isSymbol(c)){
                if (tempNode == rootNode){
                    result.append(c);
                    begin++;
                }
                position++;
                continue;
            }

            tempNode = tempNode.getSubNode(c);
            if (tempNode == null){
                result.append(text.charAt(begin));
                begin = begin + 1;
                position = begin;
                tempNode = rootNode;
            }else if (tempNode.isKeywordEnd()){
                result.append(replacement);
                begin = position + 1;
                position = begin;
                tempNode = rootNode;
            }else {
                position++;
                if (position == text.length()){
                    result.append(text.charAt(begin));
                    begin++;
                    position = begin;
                    tempNode = rootNode;
                }
            }
        }
        result.append(text.substring(begin));
        return result.toString();
    }

    /**
     * 添加敏感词到字典树
     * @param lineTxt
     */
    public void addWord(String lineTxt){
        TrieNode tempNode = rootNode;

        for (int i = 0; i < lineTxt.length(); i++) {
            Character c = lineTxt.charAt(i);
            /**
             * 过滤掉符号干扰
             */
            if (isSymbol(c)){
                continue;
            }

            TrieNode node = tempNode.getSubNode(c);
            if (node == null){
                node = new TrieNode();
                tempNode.setSubNode(c,node);
            }

            tempNode = node;

            if (i == lineTxt.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }
    }



    /**
     * 判断是否是一个符号
     * @param c
     * @return
     */
    public boolean isSymbol(char c){
        int ic = (int)c;
        //0x2E80~0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (ic < 0x2E80 || ic > 0x9FFF);
    }



    private class TrieNode{

        /**
         * 用来存储子节点
         */
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        /**
         * 用来表示敏感词的终结，true为终结
         */
        private boolean end = false;

        /**
         * 设置子节点
         * @param key
         * @param node
         */
        public void setSubNode(Character key,TrieNode node){
            subNodes.put(key,node);
        }

        /**
         * 获取子节点
         * @param key
         * @return
         */
        public TrieNode getSubNode(Character key){
            return subNodes.get(key);
        }

        /**
         * 设置本节点是否是敏感词的终结
         * @param end
         */
        public void setKeywordEnd(boolean end){
            this.end = end;
        }

        /**
         * 判断是否是敏感词的终结
         * @return
         */
        public boolean isKeywordEnd(){
            return this.end;
        }

        /**
         * 获取子节点的数量
         * @return
         */
        public int getSubNodeCount(){
            return subNodes.size();
        }

    }

}
