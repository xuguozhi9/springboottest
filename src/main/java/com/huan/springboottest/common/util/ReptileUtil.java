package com.huan.springboottest.common.util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @Description: 爬虫工具类
 * @Author: Huan
 * @CreateTime: 2019-02-17 15:13
 */
public class ReptileUtil {

    /* 通过选择器来选取页面的 */
    public static Elements select(Document doc, String cssSelector) {
        return doc.select(cssSelector);
    }

    /*
     *  通过css选择器来得到指定元素;
     *
     *  */
    public static Element select(Document doc, String cssSelector, int index) {
        Elements eles = select(doc, cssSelector);
        int realIndex = index;
        if (index < 0) {
            realIndex = eles.size() + index;
        }
        return eles.get(realIndex);
    }


    /**
     * 获取满足选择器的元素中的链接 选择器cssSelector必须定位到具体的超链接
     * 例如我们想抽取id为content的div中的所有超链接，这里
     * 就要将cssSelector定义为div[id=content] a
     * 放入set 中 防止重复；
     *
     * @param cssSelector
     * @return
     */
    public static Set<String> getLinks(Document doc, String cssSelector) {
        Set<String> links = new HashSet<String>();
        Elements es = select(doc, cssSelector);
        Iterator iterator = es.iterator();
        while (iterator.hasNext()) {
            Element element = (Element) iterator.next();
            if (element.hasAttr("href")) {
                links.add(element.attr("abs:href"));
            } else if (element.hasAttr("src")) {
                links.add(element.attr("abs:src"));
            }
        }
        return links;
    }


    /**
     * 获取网页中满足指定css选择器的所有元素的指定属性的集合
     * 例如通过getAttrs("img[src]","abs:src")可获取网页中所有图片的链接
     *
     * @param cssSelector
     * @param attrName
     * @return
     */
    public static ArrayList<String> getAttrs(Document doc, String cssSelector, String attrName) {
        ArrayList<String> result = new ArrayList<String>();
        Elements eles = select(doc, cssSelector);
        for (Element ele : eles) {
            if (ele.hasAttr(attrName)) {
                result.add(ele.attr(attrName));
            }
        }
        return result;
    }

    public static List<String> getHtml(Document doc, String cssSelector){
        List<String> list = new ArrayList<>();
        Elements els = select(doc,cssSelector);
        for (Element ele : els) {
            list.add(ele.html());
        }
        return list;
    }

    public static List<Node> getNodes(Document doc, String cssSelector){
        List<Node> nodeList = new ArrayList<>();
        Elements eles = select(doc, cssSelector);
        for (Element ele : eles) {

        }
        return nodeList;
    }
}
