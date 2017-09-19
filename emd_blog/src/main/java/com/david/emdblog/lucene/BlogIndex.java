package com.david.emdblog.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.david.emdblog.constant.Constants;
import com.david.emdblog.entity.Blog;
import com.david.emdblog.service.BlogService;
import com.david.emdblog.service.impl.BloggerServiceImpl;
import com.david.emdblog.utils.DateUtil;
import com.david.emdblog.utils.UtilFuns;

/**
 * 博客索引类
 * 
 * @Author ：程序员小冰
 * @新浪微博 ：http://weibo.com/mcxiaobing
 * @GitHub: https://github.com/QQ986945193
 */
public class BlogIndex {

	/**
	 * 更新文章博客索引
	 */
	public void updateIndex(Blog blog) throws Exception {
		IndexWriter writer = getWriter();
		Document document = new Document();
		document.add(new StringField("id", String.valueOf(blog.getId()), Field.Store.YES));
		document.add(new TextField("title", blog.getTitle(), Field.Store.YES));
		document.add(new StringField("releaseDate", DateUtil.formatDate(new Date(), "yyyy-MM-dd"), Field.Store.YES));
		document.add(new TextField("content", blog.getContentNoTag(), Field.Store.YES));
		writer.updateDocument(new Term("id", String.valueOf(blog.getId())), document);
		if (writer != null) {
			writer.close();
		}
	}

	/**
	 * 删除指定文章博客的索引
	 */
	public void deleteIndex(String blogId) throws Exception {
		IndexWriter indexWriter = getWriter();
		indexWriter.deleteDocuments(new Term("id", blogId));
		indexWriter.forceMergeDeletes();// 强制删除
		indexWriter.commit();
		if (indexWriter != null) {
			indexWriter.close();
		}
	}

	/**
	 * 添加博客索引
	 */
	public void addIndex(Blog blog) throws Exception {
		IndexWriter writer = getWriter();
		Document document = new Document();
		document.add(new StringField("id", String.valueOf(blog.getId()), Field.Store.YES));
		document.add(new TextField("title", blog.getTitle(), Field.Store.YES));
		document.add(new StringField("releaseDate", DateUtil.formatDate(new Date(), "yyyy-MM-dd"), Field.Store.YES));
		document.add(new TextField("content", blog.getContentNoTag(), Field.Store.YES));
		writer.addDocument(document);
		if (writer != null) {
			writer.close();

		}
	}

	private Directory directory = null;

	/**
	 * 获取IndexWriter实例
	 */

	private IndexWriter getWriter() throws Exception {
		directory = FSDirectory.open(Paths.get(Constants.constant_LUCENE));
		SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		return indexWriter;
	}

	/**
	 * 查询博客信息
	 * 
	 * @param q
	 *            查询关键字
	 * @return
	 * @throws Exception
	 */
	public List<Blog> searchBlog(String q) throws Exception {
		IndexWriter writer = getWriter();
		directory = FSDirectory.open(Paths.get(Constants.constant_LUCENE));
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher is = new IndexSearcher(reader);
		BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
		SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
		QueryParser parser = new QueryParser("title", analyzer);
		Query query = parser.parse(q);
		QueryParser parser2 = new QueryParser("content", analyzer);
		Query query2 = parser2.parse(q);
		booleanQuery.add(query, BooleanClause.Occur.SHOULD);
		booleanQuery.add(query2, BooleanClause.Occur.SHOULD);
		TopDocs hits = is.search(booleanQuery.build(), 100);
		QueryScorer scorer = new QueryScorer(query);
		Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
		SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color='red'>", "</font></b>");
		Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
		highlighter.setTextFragmenter(fragmenter);
		List<Blog> blogList = new LinkedList<Blog>();
		for (ScoreDoc scoreDoc : hits.scoreDocs) {
			Document doc = is.doc(scoreDoc.doc);
			Blog blog = new Blog();
			blog.setId(Integer.parseInt(doc.get(("id"))));
			blog.setReleaseDateStr(doc.get(("releaseDate")));
			String title = doc.get("title");
			String content = doc.get("content");
			if (title != null) {
				TokenStream tokenStream = analyzer.tokenStream("title", new StringReader(title));
				String hTitle = highlighter.getBestFragment(tokenStream, title);
				if (UtilFuns.isEmpty(hTitle)) {
					blog.setTitle(title);
				} else {
					blog.setTitle(hTitle);
				}
			}
			if (content != null) {
				TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(content));
				String hContent = highlighter.getBestFragment(tokenStream, content);
				if (UtilFuns.isEmpty(hContent)) {
					if (content.length() <= 200) {
						blog.setContent(content);
					} else {
						blog.setContent(content.substring(0, 200));
					}
				} else {
					blog.setContent(hContent);
				}
			}
			blogList.add(blog);
		}
		return blogList;
	}

	public static void main(String[] args) throws Exception {
//		IndexWriter writer = new BlogIndex().getWriter();
//		writer.close();
//		System.out.println(Paths.get(Constants.constant_LUCENE));
//		Directory directory = FSDirectory.open(Paths.get(Constants.constant_LUCENE));
//		IndexReader reader = DirectoryReader.open(directory);
//		System.out.println(reader);
		BlogService blogService = (BlogService) new BloggerServiceImpl();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("start",0);// 开始第几页，
		map.put("size",50);// 每页展示数量
		List<Blog> lists = blogService.list(map);
		for (Blog blog : lists) {
			new BlogIndex().addIndex(blog);
		}
	}
}