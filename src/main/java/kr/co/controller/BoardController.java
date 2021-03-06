package kr.co.controller;

import java.io.File;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.co.service.BoardService;
import kr.co.service.ReplyService;
import kr.co.vo.BoardVO;
import kr.co.vo.Criteria;
import kr.co.vo.PageMaker;
import kr.co.vo.ReplyVO;
import kr.co.vo.SearchCriteria;

@Controller
@RequestMapping("/board/*")
public class BoardController {
	
	private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
	
	@Inject
	BoardService service;
	
	@Inject
	ReplyService replyService;
	
	// 게시판 글 작성 화면
		@RequestMapping(value = "/board/writeView", method = RequestMethod.GET)
		public void writeView() throws Exception{
			logger.info("writeView");
			
		}
	
		// 게시판 글 작성
		@RequestMapping(value = "/board/write", method = RequestMethod.POST)
		public String insert(BoardVO boardVO, MultipartHttpServletRequest mpRequest) throws Exception{
			logger.info("insert");
			
			service.insert(boardVO, mpRequest);
			return "redirect:/board/list";
		}
		

		// 게시판 목록 조회
		@RequestMapping(value = "/list", method = RequestMethod.GET)
		public String list(Model model, SearchCriteria scri) throws Exception{
			logger.info("list");
			
			model.addAttribute("list",service.list(scri));
			model.addAttribute("scri", scri); // 이거 안 적어줘서 a href 링크에서 ${scri.page}의 값을 받아오지 못 했던 거였음. 아 염병 2020.09.14
			PageMaker pageMaker = new PageMaker();
			pageMaker.setCri(scri);

			pageMaker.setTotalCount(service.listCount(scri));
			
			model.addAttribute("pageMaker", pageMaker);
			
			return "board/list";
		}
		
		// 게시판 글 조회
		@RequestMapping(value = "/readView", method = RequestMethod.GET)
		public String read(BoardVO boardVO, @ModelAttribute("scri") SearchCriteria scri, Model model) throws Exception{
			logger.info("read");
			
			model.addAttribute("read",service.read(boardVO.getBno()));
			model.addAttribute("scri", scri);
			
			List<ReplyVO> replyList = replyService.readReply(boardVO.getBno());
			model.addAttribute("replyList", replyList);
			
			List<Map<String,Object>> fileList = service.selectFileList(boardVO.getBno());
			model.addAttribute("file", fileList);
			return "board/readView";
		}
		
		// 게시판 수정뷰
		@RequestMapping(value = "/updateView", method = RequestMethod.GET)
		public String updateView(BoardVO boardVO, @ModelAttribute("scri") SearchCriteria scri, Model model) throws Exception{
			logger.info("updateView");
			model.addAttribute("update", service.read(boardVO.getBno()));
			model.addAttribute("scri", scri);
			List<Map<String, Object>> fileList = service.selectFileList(boardVO.getBno());
			model.addAttribute("file", fileList);
			
			return "board/updateView";
		}
		
		// 게시판 글 수정
		@RequestMapping(value = "/update", method = RequestMethod.POST)
		public String update(BoardVO boardVO, 
							@ModelAttribute("scri") SearchCriteria scri, 
							RedirectAttributes rttr,
							@RequestParam(value="fileNoDel[]") String[] files,
							@RequestParam(value="fileNameDel[]") String[] fileNames,
							MultipartHttpServletRequest ucRequest) throws Exception{
			logger.info("update");
			service.update(boardVO, files, fileNames, ucRequest);
			
			rttr.addAttribute("page", scri.getPage());
			rttr.addAttribute("perPageNum", scri.getPerPageNum());
			rttr.addAttribute("searchType", scri.getSearchType());
			rttr.addAttribute("keyword", scri.getKeyword());
			return "redirect:/board/list";
		}
		
		// 게시판 글 삭제
		@RequestMapping(value = "/delete", method = RequestMethod.POST)
		public String delete(BoardVO boardVO, @ModelAttribute("scri") SearchCriteria scri, RedirectAttributes rttr) throws Exception{
			logger.info("delete");
			service.delete(boardVO.getBno());
			
			rttr.addAttribute("page", scri.getPage());
			rttr.addAttribute("perPageNum", scri.getPerPageNum());
			rttr.addAttribute("searchType", scri.getSearchType());
			rttr.addAttribute("keyword", scri.getKeyword());
			return "redirect:/board/list";
		}
		
		// 댓글 작성 
		@RequestMapping(value = "/replyWrite", method = RequestMethod.POST)
		public String replyWrite(ReplyVO vo, SearchCriteria scri, RedirectAttributes rttr) throws Exception {
			logger.info("reply write");
			
			replyService.writeReply(vo);
			
			rttr.addAttribute("bno", vo.getBno());
			rttr.addAttribute("page", scri.getPage());
			rttr.addAttribute("perPageNum", scri.getPerPageNum());
			rttr.addAttribute("searchType", scri.getSearchType());
			rttr.addAttribute("keyword", scri.getKeyword());
			return "redirect:/board/readView";
		}
		
		// 댓글 수정(댓글 수정페이지 접근용) 
		@RequestMapping(value ="/replyUpdateView", method = RequestMethod.GET)
		public String replyUpdateView(ReplyVO vo, SearchCriteria scri, Model model) throws Exception {
			logger.info("reply Update View");
			
			model.addAttribute("replyUpdate", replyService.selectReply(vo.getRno()));
			model.addAttribute("scri", scri);
			
			return "board/replyUpdateView";
		}
		
		
		// 댓글 수정(댓글 수정한 값 전달용)
		@RequestMapping(value = "/replyUpdate", method = RequestMethod.POST)
		public String replyUpdate(ReplyVO vo, SearchCriteria scri, RedirectAttributes rttr) throws Exception {
			logger.info("reply Update");
			
			replyService.updateReply(vo);
			
			rttr.addAttribute("bno", vo.getBno());
			rttr.addAttribute("page", scri.getPage());
			rttr.addAttribute("perPageNum", scri.getPerPageNum());
			rttr.addAttribute("searchType", scri.getSearchType());
			rttr.addAttribute("keyword", scri.getKeyword());
			
			return "redirect:/board/readView";
		}
		
		
		// 댓글 삭제 GET(댓글 삭제페이지 접근용)
		@RequestMapping(value = "/replyDeleteView", method = RequestMethod.GET) 
		public String replyDeleteView(ReplyVO vo, SearchCriteria scri, Model model) throws Exception {
			logger.info("reply Delete View");
			
			model.addAttribute("replyDelete", replyService.selectReply(vo.getRno()));
			model.addAttribute("scri", scri);
			
			return "board/replyDeleteView";
		}
		
		
		// 댓글 삭제 POST(댓글 삭제용)
		@RequestMapping(value = "/replyDelete", method = RequestMethod.POST)
		public String replyDelete(ReplyVO vo, SearchCriteria scri, RedirectAttributes rttr) throws Exception {
			logger.info("reply Delete");
			
			replyService.deleteReply(vo);
			
			rttr.addAttribute("bno", vo.getBno());
			rttr.addAttribute("page", scri.getPage());
			rttr.addAttribute("perPageNum", scri.getPerPageNum());
			rttr.addAttribute("searchType", scri.getSearchType());
			rttr.addAttribute("keyword", scri.getKeyword());
			return "redirect:/board/readView";
		}

		// 첨부파일 다운로드
		@RequestMapping(value = "/fileDown")
		public void fileDown(@RequestParam Map<String, Object> map, HttpServletResponse response) throws Exception {
			Map<String, Object> resultMap = service.selectFileInfo(map);
			System.out.println("4 : " + resultMap);
			String storedFileName = (String) resultMap.get("stored_file_name");
			String originalFileName = (String) resultMap.get("org_file_name");
			
			// 파일을 저장했던 위치에서 첨부파일을 읽어 byte[] 형식으로 변환한다.
			System.out.println("/Users/juwonlee/Desktop/"+storedFileName);
			byte fileByte[] = org.apache.commons.io.FileUtils.readFileToByteArray(new File("/Users/juwonlee/Desktop/"+storedFileName));
			
			response.setContentType("application/octet-stream");
			response.setContentLength(fileByte.length);
			response.setHeader("Content-Disposition", "attachment; fileName=\""+URLEncoder.encode(originalFileName, "UTF-8")+"\";");
			response.getOutputStream().write(fileByte);
			response.getOutputStream().flush();
			response.getOutputStream().close();
		}
 }
