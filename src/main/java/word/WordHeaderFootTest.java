package word;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TextAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.xmlbeans.impl.xb.xmlschema.SpaceAttribute;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFldChar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFonts;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTabStop;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STFldCharType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHdrFtr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTabJc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * 页眉和页脚的设置
 * @Author: wb_xugz
 * @CreateTime: 2020-05-07 16:09
 */
@Slf4j
public class WordHeaderFootTest {

    public void test1(){
        XWPFDocument doc = new XWPFDocument();
        XWPFTable table = doc.createTable(1, 1);
        XWPFParagraph paragraph = table.getRow(1).getCell(1).getParagraphs().get(0);
        XWPFRun run = paragraph.createRun();
        XWPFTableCell cell = table.getRow(1).getCell(1);
        XWPFParagraph xwpfParagraph = cell.addParagraph();
        CTP ctp = xwpfParagraph.getCTP();
    }

    public static void main(String[] args) throws Exception{
        XWPFDocument doc = new XWPFDocument();

        XWPFParagraph p = doc.createParagraph();



        XWPFRun r = p.createRun();
        r.setText("Some Text");
        r.setBold(true);
        r.addBreak(BreakType.PAGE);
        r = p.createRun();
        r.setText("Goodbye");

        createHeader(doc,"公司", "2");
        createFooter(doc, "211", "23");

        OutputStream os = new FileOutputStream(new File("header2.docx"));
        doc.write(os);
        os.close();
        doc.close();
    }

    public static void createHeader(XWPFDocument doc, String orgFullName, String logoFilePath) throws Exception {
        /*
         * 对页眉段落作处理，使公司logo图片在页眉左边，公司全称在页眉右边
         * */
        CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
        XWPFHeaderFooterPolicy headerFooterPolicy = new XWPFHeaderFooterPolicy(doc, sectPr);
        XWPFHeader header = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);

        XWPFParagraph paragraph;
        paragraph = header.createParagraph();
        //XWPFParagraph paragraph = header.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        paragraph.setBorderBottom(Borders.THICK);

        CTTabStop tabStop = paragraph.getCTP().getPPr().addNewTabs().addNewTab();
        tabStop.setVal(STTabJc.RIGHT);
        int twipsPerInch = 1440;
        tabStop.setPos(BigInteger.valueOf(6 * twipsPerInch));

        XWPFRun run = paragraph.createRun();
        setXWPFRunStyle(run, "新宋体", 10);

        /*
         * 根据公司logo在ftp上的路径获取到公司到图片字节流
         * 添加公司logo到页眉，logo在左边
         * */
        if (StringUtils.isNotEmpty(logoFilePath)) {
            String imgFile = "logo.jpg";
            byte[] bs = getBytes("logo.jpg");
            InputStream is = new ByteArrayInputStream(bs);

            XWPFPicture picture = run.addPicture(is, XWPFDocument.PICTURE_TYPE_JPEG, imgFile, Units.toEMU(80), Units.toEMU(45));

//            String blipID = "";
//            for (XWPFPictureData picturedata : header.getAllPackagePictures()) {    //这段必须有，不然打开的logo图片不显示
//                blipID = header.getRelationId(picturedata);
//            }
//            picture.getCTPicture().getBlipFill().getBlip().setEmbed(blipID);
            run.addTab();
            is.close();
        }

        /*
         * 添加字体页眉，公司全称
         * 公司全称在右边
         * */
        if (StringUtils.isNotEmpty(orgFullName)) {
            run = paragraph.createRun();
            run.setText(orgFullName);
            setXWPFRunStyle(run, "新宋体", 10);
        }
    }


    public static void createFooter(XWPFDocument document, String telephone, String orgAddress) throws Exception {
        /*
         * 生成页脚段落
         * 给段落设置宽度为占满一行
         * 为公司地址和公司电话左对齐，页码右对齐创造条件
         * */
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        XWPFHeaderFooterPolicy headerFooterPolicy = new XWPFHeaderFooterPolicy(document, sectPr);
        XWPFFooter footer = headerFooterPolicy.createFooter(STHdrFtr.DEFAULT);
        XWPFParagraph paragraph = footer.createParagraph();
        //XWPFParagraph paragraph = footer.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        paragraph.setVerticalAlignment(TextAlignment.CENTER);
        paragraph.setBorderTop(Borders.THICK);
        CTTabStop tabStop = paragraph.getCTP().getPPr().addNewTabs().addNewTab();
        tabStop.setVal(STTabJc.RIGHT);
        int twipsPerInch = 1440;
        tabStop.setPos(BigInteger.valueOf(6 * twipsPerInch));

        /*
         * 给段落创建元素
         * 设置元素字面为公司地址+公司电话
         * */
        XWPFRun run = paragraph.createRun();
        run.setText((StringUtils.isNotEmpty(orgAddress) ? orgAddress : "") + (StringUtils.isNotEmpty(telephone) ? "  " + telephone : ""));
        setXWPFRunStyle(run, "仿宋", 10);
        run.addTab();

        /*
         * 生成页码
         * 页码右对齐
         * */
        run = paragraph.createRun();
        run.setText("第");
        setXWPFRunStyle(run, "仿宋", 10);

        run = paragraph.createRun();
        CTFldChar fldChar = run.getCTR().addNewFldChar();
        fldChar.setFldCharType(STFldCharType.Enum.forString("begin"));

        run = paragraph.createRun();
        CTText ctText = run.getCTR().addNewInstrText();
        ctText.setStringValue("PAGE  \\* MERGEFORMAT");
        ctText.setSpace(SpaceAttribute.Space.Enum.forString("preserve"));
        setXWPFRunStyle(run, "仿宋", 10);

        fldChar = run.getCTR().addNewFldChar();
        fldChar.setFldCharType(STFldCharType.Enum.forString("end"));

        run = paragraph.createRun();
        run.setText("页 总共");
        setXWPFRunStyle(run, "仿宋", 10);

        run = paragraph.createRun();
        fldChar = run.getCTR().addNewFldChar();
        fldChar.setFldCharType(STFldCharType.Enum.forString("begin"));

        run = paragraph.createRun();
        ctText = run.getCTR().addNewInstrText();
        ctText.setStringValue("NUMPAGES  \\* MERGEFORMAT ");
        ctText.setSpace(SpaceAttribute.Space.Enum.forString("preserve"));
        setXWPFRunStyle(run, "仿宋", 10);

        fldChar = run.getCTR().addNewFldChar();
        fldChar.setFldCharType(STFldCharType.Enum.forString("end"));

        run = paragraph.createRun();
        run.setText("页");
        setXWPFRunStyle(run, "仿宋", 10);

    }

    /**
     * 设置页脚的字体样式
     * @param r1 段落元素
     */
    private static void setXWPFRunStyle(XWPFRun r1, String font, int fontSize) {
        r1.setFontSize(fontSize);
        CTRPr rpr = r1.getCTR().isSetRPr() ? r1.getCTR().getRPr() : r1.getCTR().addNewRPr();
        CTFonts fonts = rpr.isSetRFonts() ? rpr.getRFonts() : rpr.addNewRFonts();
        fonts.setAscii(font);
        fonts.setEastAsia(font);
        fonts.setHAnsi(font);
    }

    public static byte[] getBytes(String path){
        byte[] data = null;
        FileInputStream fis = null;
        ByteArrayOutputStream os = null;
        try {
            fis = new FileInputStream(new File(path));
            os = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while ((numBytesRead = fis.read(buf)) != -1) {
                os.write(buf, 0, numBytesRead);
            }
            data = os.toByteArray();
        } catch (Exception e) {
            log.error("【文件工具】文件到byte数组:" + e.getMessage(), e);
        } finally {
            try {
                if (fis != null)
                    fis.close();
                if (os != null)
                    os.close();
            } catch (IOException e) {
                log.error("【文件工具】文件流关闭失败:" + e.getMessage(), e);
            }
        }
        return data;
    }

}
