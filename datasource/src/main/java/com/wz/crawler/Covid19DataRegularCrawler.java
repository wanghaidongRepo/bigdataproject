package com.wz.crawler;

import com.alibaba.fastjson.JSON;
import com.wz.bean.CovidBean;
import com.wz.util.HttpUtils;
import com.wz.util.TimeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Covid19DataRegularCrawler {
    @Autowired
    KafkaTemplate kafkaTemplate;
    @Scheduled(initialDelay = 1000, fixedDelay = 15000)
    public void regularCrawler() {
        long currentTimeMillis = System.currentTimeMillis();//当前系统时间毫秒值
        String datetime = TimeUtils.format(currentTimeMillis, "yyyy-MM-dd");//把毫秒值转为年-月-日格式
        //1.指定待爬取网站的URL
        String url = "https://ncov.dxy.cn/ncovh5/view/pneumonia";

        //2.使用爬虫爬取指定的url
        String html = HttpUtils.getHtml(url);
        //System.out.println(html);

        //接下来解析页面内容--获取我们该项目需要的国内疫情数据
        //3.解析html为Document,注意导包: import org.jsoup.nodes.Document;
        Document doc = Jsoup.parse(html);

        //4.获取国内疫情数据script文本(js文本)
        String scriptText = doc.select("script[id=getAreaStat]").toString();
        //System.out.println("解析到的国内的疫情数据script文本为:");
        //System.out.println(scriptText);

        //5.获取js文本中的json数据
        //准备变量用来接收获取到的json数据
        String jsonStr = "";
        //定义一个正则表达式对象用来解析出json
        String regex = "\\[(.*)\\]";//表示获取以[开头,]结尾,中间是任意多组的String
        Pattern regexPattern = Pattern.compile(regex);//将正则表达式编译成一个正则规则对象
        Matcher matcher = regexPattern.matcher(scriptText);//使用正则规则对象去scriptText文本中寻找匹配的字符串
        if (matcher.find()) {//表示在scriptText文本中找到了符合该正则规则的字符串
            jsonStr = matcher.group(0);//取出匹配到的内容
            System.out.println("匹配到的内容为:");
            System.out.println(jsonStr);
        } else {
            System.out.println("no match");
        }
        //上面获取到的jsonStr就是全国各省市自治区的疫情数据
        //机器可以很方便的识别,人眼去看需要借助工具格式化
        //https://www.sojson.com/

        //6.进一步解析jsonStr(其实是一个json数组,里面包含很多个省份(省份下面还有多个城市)),分离出更详细的内容,并封装成JavaBean,方便后续发送给Kafka,也方便后续其他程序从Kafka中获取数据并解析
        //后续可以使用阿里巴巴的fastjson工具来解析并封装为JavaBean
        //将json数组中的每一个省份(省份/特区/自治区/直辖市)解析为CovidBean
        List<CovidBean> pCovidBeans = JSON.parseArray(jsonStr, CovidBean.class);
        for (CovidBean pBean : pCovidBeans) {
            //pBean就是每一个一级的省份(省份/特区/自治区/直辖市)
            pBean.setDatetime(datetime);
            //省份(省份/特区/自治区/直辖市)的每一天的疫情数据需要单独再爬取
            String statisticsDataUrl = pBean.getStatisticsData();
            String statisticsDataJsonStr = HttpUtils.getHtml(statisticsDataUrl);
            //把该json数据再设置回pBean中
            pBean.setStatisticsData(statisticsDataJsonStr);
            System.out.println(pBean);

            //取出pBean省份(省份/特区/自治区/直辖市)中的citys并将city也封装成CovidBean
            String citiesJsonStr = pBean.getCities();
            List<CovidBean> covidBeans = JSON.parseArray(citiesJsonStr, CovidBean.class);
            for (CovidBean bean : covidBeans) {
                //bean就是该省份(省份/特区/自治区/直辖市)下的城市(市/区)
                bean.setDatetime(datetime);//设置时间
                bean.setProvinceShortName(pBean.getProvinceShortName());//设置该城市所属省份的短名
                bean.setPid(pBean.getLocationId());//设置该城市的父pid为省份的id
                System.out.println(bean);
                String pBeanJson = JSON.toJSONString(bean);
                kafkaTemplate.send("covid19",bean.getPid(),pBeanJson);
            }
            //可以把pBean中的citys设置为null,因为上面已经处理过了
            pBean.setCities(null);
            String pBeanJson = JSON.toJSONString(pBean);
            kafkaTemplate.send("covid19",pBean.getLocationId(),pBeanJson);

            //注意1:
            //当然目前是属于数据爬取测试阶段,后续需要将数据再转为json发送给Kafka
            //前面经历的json-->转为CovidBean,进行了数据的初步处理/预处理,再转为json发送给Kafka,减少了后续从Kafka中获取数据的处理成本
            //跨网络的数据传输,一般都可以使用json,比较方便

            //注意2:
            //现在的爬虫是需要我们手动去启动的,实际中可以设置为定时任务
            //如:每隔12/24小时爬取一次
            //如:每个工作日早上10点开始爬取,每天爬一次...
            //后续可以使用JDK中的定时任务API(不好用),也可以使用SpringBoot提供的定时任务API(较为常用)
        }
    }
}
