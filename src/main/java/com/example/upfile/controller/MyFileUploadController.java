package com.example.upfile.controller;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.example.upfile.entity.FileContent;
import com.example.upfile.entity.MyUploadForm;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jxls.reader.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

/**
 * Nam on 10/11/2018
 */
@RestController
public class MyFileUploadController {

    // POST: Sử lý Upload
    @RequestMapping(value = "/up", method = RequestMethod.POST)
    public String uploadOneFileHandlerPOST(@RequestBody MultipartFile multipartFile) {

        try {
            execute(multipartFile.getInputStream());
            return "done";
        } catch (IOException e) {
            e.printStackTrace();
        } catch (org.apache.poi.openxml4j.exceptions.InvalidFormatException e) {
            e.printStackTrace();
        }
        return "fails";
    }

    public void execute(InputStream stream) throws IOException, org.apache.poi.openxml4j.exceptions.InvalidFormatException {
        InputStream inputXLS = new BufferedInputStream(stream);
        Workbook hssfInputWorkbook = WorkbookFactory.create(inputXLS);
        Sheet sheet = hssfInputWorkbook.getSheetAt( 0 );
        List mappings = new ArrayList();
        List<FileContent> fileContent = new ArrayList<>();
        Map beans = new HashMap();
        beans.put("fileContents", fileContent);
        mappings.add( new BeanCellMapping(0, (short) 0, "fileContent", "local_url"));
        mappings.add( new BeanCellMapping(0, (short) 1, "fileContent", "apk_name"));
        XLSBlockReader reader = new SimpleBlockReaderImpl(0, 0, mappings);
        XLSRowCursor cursor = new XLSRowCursorImpl( sheet );
        XLSLoopBlockReader forEachReader = new XLSForEachBlockReaderImpl(0, 0, "fileContents", "fileContent", FileContent.class);
        forEachReader.addBlockReader( reader );
        SectionCheck loopBreakCheck = getLoopBreakCheck();
        forEachReader.setLoopBreakCondition( loopBreakCheck );
        cursor.setCurrentRowNum(1);

        forEachReader.read( cursor, beans );
        fileContent.stream().forEach(f -> System.out.println(f.getLocal_url()));
    }
    private SectionCheck getLoopBreakCheck() {
        OffsetRowCheck rowCheck = new OffsetRowCheckImpl( 0 );
        rowCheck.addCellCheck( new OffsetCellCheckImpl((short) 0, "") );
        SectionCheck sectionCheck = new SimpleSectionCheck();
        sectionCheck.addRowCheck( rowCheck );
        return sectionCheck;
    }
}
