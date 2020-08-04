package com.wz.crawler;

import com.wz.utils.HttpUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.File;

public class JsoupTest {
    //如果@Test用不了,可以写main进行测试
    public static void main(String[] args) {

    }

    //--1.测试Jsoup获取Html文档
    @Test
    public void testGetDocument() throws Exception {
        //注意:虽然Jsoup可以直接对网站发起请求,但是功能有限,实际中还是需要使用HttpClient来进行数据爬取
        //Document htmlDoc = Jsoup.connect("http://www.itcast.cn/").get();
        //Document htmlDoc = Jsoup.parse(new URL("http://www.itcast.cn/"),10000);

        //下面这种方式可以获取本地的HTML文件方便后续做解析,学习jsoup测试可以使用
        //Document htmlDoc = Jsoup.parse(new File("D:\\jsoup.html"), "UTF-8");

        //开发中使用下面的方式
        //1.使用HttpClient爬取网页内容
        String html = HttpUtils.getHtml("http://www.itcast.cn/");
        //2.使用Jsoup解析网页内容为Document文档对象
        Document htmlDoc = Jsoup.parse(html);
        //System.out.println(htmlDoc);
        //3.可以根据需要获取自己想要的内容,如获取标题
        String title = htmlDoc.getElementsByTag("title").first().text();
        System.out.println("解析出来的html文档的标题为:" + title);

    }

    //jsoup解析网页内容的API也很多,接下来进行演示
    //但是注意:项目中不一定全部用到,但是可以了解一下,以后如果要自己爬取其他内容可以灵活选用API
    //--2.演示jsoup其他API-获取元素
    @Test
    public void testGetElement() throws Exception {
        //1.获取Document,为了方便学习测试,这里先解析本地的一个简单的html文档,D:\\jsoup.html
        Document doc = Jsoup.parse(new File("C:\\Users\\qianl\\Documents\\Tencent Files\\382134677\\" +
                                                      "FileRecv\\jsoup.html"), "UTF-8");
        //2.根据id获取元素
        Element city_bj = doc.getElementById("city_bj");
        System.out.println(city_bj.text());//北京中心
        //3.根据标签名获取元素
        Elements spans = doc.getElementsByTag("span");
        for (Element span : spans) {
            System.out.println(span.text());//北京 上海  广州
        }
        //4.根据class获取元素
        Elements s_names = doc.getElementsByClass("s_name");
        for (Element s_name : s_names) {
            System.out.println(s_name.text());//北京 上海  广州
        }
        //5.根据属性获取元素
        Element abc = doc.getElementsByAttribute("abc").first();
        System.out.println(abc.text());//广州
        //6.根据属性和值获取元素
        Element abc123 = doc.getElementsByAttributeValue("abc", "123").first();
        System.out.println(abc123.text());

    }

    //--3.演示jsoup其他API-获取元素中的内容
    @Test
    public void testGetContent() throws Exception {
        //1.获取Document和Element,为了方便学习测试,这里先解析本地的一个简单的html文档,D:\\jsoup.html
        Document doc = Jsoup.parse(new File("C:\\Users\\qianl\\Documents\\Tencent Files\\" +
                                            "382134677\\FileRecv\\jsoup.html"), "UTF-8");
        Element city_bj = doc.getElementById("city_bj");

        //2.获取元素的id
        System.out.println(city_bj.id());//city_bj

        //3.获取文本内容
        System.out.println(city_bj.text());//北京中心 //只会获取Element里面的文本内容

        //4.获取classname
        System.out.println(city_bj.className());//获取到自己添加的classname //class="xxclass"

        //5.获取指定的属性
        System.out.println(city_bj.attr("key1"));//获取到自己额外添加的属性 //key1="value1"

        //6.获取html
        System.out.println(city_bj.html());//如果Element元素中有html标签内容会原样获取
    }

    //通过上面的Document获取+ Element获取+ 内容获取 就可以完一些简单的页面解析
    //但是实际中页面会比较复杂,上面的API很灵活但是不够简洁,所以jsoup还退出了选择器解析,使用起来更加简单

    //--4.演示jsoup其他API-选择器
    @Test
    public void testSelector() throws Exception {
        //1.获取Document,为了方便学习测试,这里先解析本地的一个简单的html文档,D:\\jsoup.html
        Document doc = Jsoup.parse(new File("C:\\Users\\qianl\\Documents\\Tencent Files\\" +
                "                               382134677\\FileRecv\\jsoup.html"), "UTF-8");
        //2.标签选择器
        //Elements spans = doc.getElementsByTag("span");//以前的写法
        Elements spans = doc.select("span");
        for (Element span : spans) {
            System.out.println(span.text());//北京 上海 广州
        }
        //3.id选择器
        Elements city_bj = doc.select("#city_bj");//注意:#表示id选择器
        System.out.println(city_bj.first().text());//北京中心

        //4.class属性选择器
        Elements s_names = doc.select(".s_name");//注意:.表示class属性
        for (Element s_name : s_names) {
            System.out.println(s_name.text());//北京 上海 广州
        }
        //5.其他属性选择器
        Elements abc = doc.select("[abc]");//注意:[]表示属性选择器
        System.out.println(abc.first().text());//广州

        //6.指定属性名和属性值
        Elements abc456 = doc.select("[abc=456]");
        System.out.println(abc456.first().text());//深圳

        System.out.println("==============================");
        //7.选择器组合使用
        Elements eles = doc.select("li#test");//获取id为test的li标签
        System.out.println(eles.text());//北京

        Elements eles2 = doc.select(".city_con li");//获取class为city_con的下面的所有的li
        System.out.println(eles2.text());//北京 上海 广州 深圳 天津

        Elements eles3 = doc.select(".city_con > ul > li");//获取class为city_con下的直接的ul下的直接的li
        System.out.println(eles3.text());//北京 上海 广州 深圳

        Elements eles4 = doc.select(".city_con > ul > ul > li");//获取class为city_con下的直接的ul下的直接的ul下的直接的li
        System.out.println(eles4.text());//天津
    }

    //--5通过前面的学习基本上已经学会了较为复杂的html解析,那么接下来完成一个小练习
    //获取http://www.itcast.cn/官网左侧大学起点的学科有哪些?
    @Test
    public void testGetSubject() throws Exception {
        //1.爬取
        String html = HttpUtils.getHtml("http://www.itcast.cn/");
        //2.解析为doc
        Document doc = Jsoup.parse(html);
        //3.获取我们想要的内容
        Elements subjects = doc.select(".ulon .a_gd");
        for (Element subject : subjects) {
            System.out.println(subject.text());
        }
        //JavaEE
        //HTML&JS+前端 
        //大数据
        //Python+人工智能
        //UI/UE设计
        //软件测试
        //C/C++
        //新媒体+短视频直播运营
        //产品经理
        //Linux云计算+运维开发 
        //拍摄剪辑
        //智能机器人软件开发
        //电商视觉运营设计
    }
}
