package com.example.upfile.controller;

import com.example.upfile.entity.FileContent;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jxls.reader.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nam on 10/11/2018
 */
@RestController
public class MyFileUploadController {

    // POST: Sử lý Upload
    @PostMapping("/up")
    public String uploadOneFileHandlerPOST(@RequestBody MultipartFile multipartFile) {

        try {
            execute(multipartFile.getInputStream());
            return "done";
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "fails";
    }

    public void execute(InputStream stream) throws IOException, org.apache.poi.openxml4j.exceptions.InvalidFormatException {
        InputStream inputXLS = new BufferedInputStream(stream);
        Workbook hssfInputWorkbook = WorkbookFactory.create(inputXLS);
        Sheet sheet = hssfInputWorkbook.getSheetAt(0);
        List<BeanCellMapping> mappings = new ArrayList<>();
        List<FileContent> fileContent = new ArrayList<>();
        Map<String, List<FileContent>> beans = new HashMap<>();
        beans.put("fileContents", fileContent);
        mappings.add(new BeanCellMapping(0, (short) 0, "fileContent", "local_url"));
        mappings.add(new BeanCellMapping(0, (short) 1, "fileContent", "apk_name"));
        XLSBlockReader reader = new SimpleBlockReaderImpl(0, 0, mappings);
        XLSRowCursor cursor = new XLSRowCursorImpl(sheet);
        XLSLoopBlockReader forEachReader = new XLSForEachBlockReaderImpl(0, 0, "fileContents", "fileContent", FileContent.class);
        forEachReader.addBlockReader(reader);
        SectionCheck loopBreakCheck = getLoopBreakCheck();
        forEachReader.setLoopBreakCondition(loopBreakCheck);
        cursor.setCurrentRowNum(1);

        forEachReader.read(cursor, beans);
        fileContent.forEach(f -> System.out.println(f.getLocal_url()));
    }

    private SectionCheck getLoopBreakCheck() {
        OffsetRowCheck rowCheck = new OffsetRowCheckImpl(0);
        rowCheck.addCellCheck(new OffsetCellCheckImpl((short) 0, ""));
        SectionCheck sectionCheck = new SimpleSectionCheck();
        sectionCheck.addRowCheck(rowCheck);
        return sectionCheck;
    }
}
