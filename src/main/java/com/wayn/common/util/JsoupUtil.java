package com.wayn.common.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * xss非法标签过滤
 * {@link http://www.jianshu.com/p/32abc12a175a?nomobile=yes}
 * @author yangwenkui
 * @version v2.0
 * @time 2017年4月27日 下午5:47:09
 */
public class JsoupUtil {

    /**
     * 使用自带的basicWithImages 白名单
     * 允许的便签有a,b,blockquote,br,cite,code,dd,dl,dt,em,i,li,ol,p,pre,q,small,span,
     * strike,strong,sub,sup,u,ul,img
     * 以及a标签的href,img标签的src,align,alt,height,width,title属性
     */
    private static final Whitelist whitelist = Whitelist.basicWithImages();
    /**
     * 配置过滤化参数,不对代码进行格式化
     */
    private static final Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);

    static {
        // 富文本编辑时一些样式是使用style来进行实现的
        // 比如红色字体 style="color:red;"
        // 所以需要给所有标签添加style属性
        whitelist.addAttributes(":all", "style", "class");
    }

    public static String clean(String content) {
        return Jsoup.clean(content, "", whitelist, outputSettings);
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        String text = "<p><br></p><table class=\"table table-bordered" +
                "\"><tbody><tr><td>11</td><td>2</td><td>3</td></tr></tbody></table><p>123</p><table class=\"table table-bordered" +
                "\"><tbody><tr><td>123</td></tr></tbody></table><p><br></p>";
        System.out.println(clean(text));

		System.out.println("------------------------");
        System.out.println(Jsoup.clean(text, Whitelist.relaxed()).trim());

	}

}
