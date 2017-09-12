package com.david.emdblog.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.david.emdblog.entity.Link;

/**
 * 友情链接管理的接口
 * @Author ：程序员小冰
 * @新浪微博 ：http://weibo.com/mcxiaobing
 * @GitHub: https://github.com/QQ986945193
 */
public interface LinkService {
	/**
	 * 获取到友情链接的列表
	 */
	public List<Link> list(Map<String, Object> map);
	/**
	 * 获取友情链接的数量
	 */
	public Long getTotal(Map<String, Object> map);
	/**
	 * 添加友情链接
	 */
	public int add(Link link);
	/**
	 * 修改更新友情链接的信息
	 */
	public int update(Link link);
	/**
	 * 删除友情链接
	 */
	public void delete(Integer id);

}
