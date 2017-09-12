package com.david.emdblog.web.controller.admin;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.david.emdblog.entity.Blog;
import com.david.emdblog.entity.BlogType;
import com.david.emdblog.entity.Blogger;
import com.david.emdblog.entity.Link;
import com.david.emdblog.service.BlogService;
import com.david.emdblog.service.BlogTypeService;
import com.david.emdblog.service.BloggerService;
import com.david.emdblog.service.LinkService;
import com.david.emdblog.utils.ResponseUtils;

import net.sf.json.JSONObject;

/**
 * 系统管理的一些controller
 * @Author ：程序员小冰
 * @新浪微博 ：http://weibo.com/mcxiaobing
 * @GitHub: https://github.com/QQ986945193
 */
@Controller
@RequestMapping("/admin/system")
public class SystemAdminController {
	
	@Resource(name="blogService")
	private BlogService blogService;

	@Resource(name="linkService")
	private LinkService linkService;
	
	@Resource(name="blogTypeService")
	private BlogTypeService blogTypeService;
	
	@Resource(name="bloggerService")
	private BloggerService bloggerService;
	/**
	 * 刷新系统缓存
	 */
	@RequestMapping("/refreshSystem")
	public String refreshSystem(HttpServletResponse response,HttpServletRequest request) throws Exception{
		ServletContext applicationContext = RequestContextUtils.getWebApplicationContext(request).getServletContext();
		Blogger blogger = bloggerService.findBloggerInfo();//查询博主信息
		blogger.setPassword(null);
		applicationContext.setAttribute("blogger", blogger);
		//查询所有文章类别以及对应的数量
		List<BlogType> blogTypeCountList = blogTypeService.countList();
		applicationContext.setAttribute("blogTypeCountList", blogTypeCountList);
		//根据日期分组查询博客,所有文章以及对应的数量
		List<Blog> blogCountList = blogService.countList();//根据日期分组查询文章。博客
		applicationContext.setAttribute("blogCountList", blogCountList);
		//获取所有友情链接
		List<Link> linkList = linkService.list(null);//获取所有友情链接
		applicationContext.setAttribute("linkList",linkList);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("success", true);
		ResponseUtils.write(response, jsonObject);
		return null;
	}
}
