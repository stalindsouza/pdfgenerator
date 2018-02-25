package pdfgenerator;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

public class PDFGenerator {
	
	enum ContentTypes 
	{ 
		NAME_VALUE("name-value"), 
		USER_TIMESTAMP_DESCRIPTION("user-timestamp-description"), 
		USER_TIMESTAMP_LINK("user-timestamp-link"), 
		LINKS("links"), 
		TEXT("text"); 
		
		private String value;
		
		ContentTypes(String value)
		{
			this.value = value;
		}
		
		static ContentTypes getEnumByValue(String matchValue)
		{
			for (ContentTypes enum_ : ContentTypes.values())
			{
				if (enum_.value.equals(matchValue)) return enum_;
			}
			return null;
		}
	};
	
	enum Styles
	{
		PAGE_WIDTH ("styles-page-width");
		
		private String value;
		
		Styles(String value)
		{
			this.value = value;
		}
		
		String valueOf()
		{
			return value;
		}
	}
	
	enum PageWidth
	{
		PAGE_WIDTH_FULL("full"),
		PAGE_WIDTH_HALF("half");
		
		private String value;
		
		PageWidth(String value)
		{
			this.value = value;
		}
		
		String valueOf()
		{
			return value;
		}
		
		static PageWidth getEnumByValue(String matchValue)
		{
			for (PageWidth ct : PageWidth.values())
			{
				if (ct.value.equals(matchValue)) return ct;
			}
			return null;
		}
	}
	
	public static final String JSON_PRINT_OUTPUT = "print_output.pdf";
	public static final String JSON_PRINT_INPUT = "print_input.json";
	
	public static void main(String[] args) throws Exception {
		
		new PDFGenerator().createPDF(
					(JSONObject) new JSONParser().parse(new FileReader(JSON_PRINT_INPUT)),
					JSON_PRINT_OUTPUT);
		
	}

	private void writePDFSection(JSONObject jsonSection, Document doc) throws Exception
	{
        ContentTypes contentType = ContentTypes.getEnumByValue((String) jsonSection.get("contents-type"));
        	JSONArray jsonArray = null;
        int count = -1;
        PdfPTable tableToAdd = null;
        switch(contentType)
        {
        case NAME_VALUE :
        		jsonArray = (JSONArray) jsonSection.get("contents");
        		tableToAdd = writePDFNameValueContent(jsonArray);
        		break;
        case TEXT:
        		break;
        case USER_TIMESTAMP_DESCRIPTION:
        		jsonArray = (JSONArray) jsonSection.get("contents");
        		count = jsonArray.size();
        		tableToAdd = writePDFUserTimestampDescriptionContent(jsonArray);
        		break;
        case USER_TIMESTAMP_LINK:
        		jsonArray = (JSONArray) jsonSection.get("contents");
        		count = jsonArray.size();
        		tableToAdd = writePDFUserTimestampLinkContent(jsonArray);
        		break;
        case LINKS:
        		jsonArray = (JSONArray) jsonSection.get("contents");
        		count = jsonArray.size();
        		tableToAdd = writePDFLinksContent(jsonArray);
        		break;
        	default :
        		throw new Exception ("unexpected contentType=" + contentType);
        }
        
        
        //
        // write header and horizontal line separator
        //
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLDITALIC);
        Font countFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
		String contentHeader = (String)jsonSection.get("header");
		
		PdfPTable table = new PdfPTable(new float[] {4,5});
		table.setWidthPercentage(100);
		table.setSpacingBefore(8);
		table.getDefaultCell().setUseAscender(true);
		table.getDefaultCell().setUseDescender(false);
		table.getDefaultCell().setUseBorderPadding(false);
		table.getDefaultCell().setPaddingBottom(0);
		
		PdfPCell headerCell = new PdfPCell(new Phrase(contentHeader, sectionFont));
		PdfPCell countCell = new PdfPCell(new Phrase((count > -1 ? count+"" : ""), countFont));
		
		headerCell.setBorder(Rectangle.NO_BORDER);
		countCell.setBorder(Rectangle.NO_BORDER);
		
		countCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		countCell.setVerticalAlignment(PdfPCell.ALIGN_BOTTOM);
		
		table.addCell(headerCell);
		table.addCell(countCell);
		
		doc.add(table);
		
        LineSeparator ls = new LineSeparator();
        doc.add(ls);
        
        doc.add(tableToAdd);
        
	}
	
	private PdfPTable writePDFUserTimestampDescriptionContent(JSONArray jsonContents) throws DocumentException 
	{
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
        
		PdfPTable table = new PdfPTable(new float[] {3,6});
		table.setWidthPercentage(100);
		table.setSpacingBefore(8);
		table.getDefaultCell().setUseAscender(true);
		table.getDefaultCell().setUseDescender(true);
		table.getDefaultCell().setUseBorderPadding(true);
		
		for (int i=0; i<jsonContents.size(); i++)
		{
			JSONObject jsonContent = (JSONObject) jsonContents.get(i);
			String contentUser = (String) jsonContent.get("user");
			String contentTimestamp = (String) jsonContent.get("timestamp");
			String contentDescription = (String) jsonContent.get("description");
			
			boolean isLastCell = (i+1 == jsonContents.size());
			int cellBorder = (isLastCell ? Rectangle.NO_BORDER : Rectangle.BOTTOM);
			int cellBottomPaddingAddFactor = (isLastCell ? 0 : 2);
			
			PdfPCell descriptionCell = new PdfPCell(new Phrase(contentDescription, valueFont));
			PdfPCell timestampCell = new PdfPCell(new Phrase(contentTimestamp + "\n" + contentUser));
			
			descriptionCell.setBorder(cellBorder);
			timestampCell.setBorder(cellBorder);
			
			descriptionCell.setPaddingBottom(descriptionCell.getPaddingBottom() + cellBottomPaddingAddFactor);
			timestampCell.setPaddingBottom(timestampCell.getPaddingBottom() + cellBottomPaddingAddFactor);
			
			table.addCell(timestampCell);
			table.addCell(descriptionCell);
		};
		
		return table;
	}
	
	private PdfPTable writePDFUserTimestampLinkContent(JSONArray jsonContents) throws DocumentException 
	{
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
        
		PdfPTable table = new PdfPTable(new float[] {3,6});
		table.setWidthPercentage(100);
		table.setSpacingBefore(8);
		table.getDefaultCell().setUseAscender(true);
		table.getDefaultCell().setUseDescender(true);
		table.getDefaultCell().setUseBorderPadding(true);
		
		for (int i=0; i<jsonContents.size(); i++)
		{
			JSONObject jsonContent = (JSONObject) jsonContents.get(i);
			String contentUser = (String) jsonContent.get("user");
			String contentTimestamp = (String) jsonContent.get("timestamp");
			String contentHypertext = (String) jsonContent.get("hypertext");
			String contentUrl = (String) jsonContent.get("url");
			
			boolean isLastCell = (i+1 == jsonContents.size());
			int cellBorder = (isLastCell ? Rectangle.NO_BORDER : Rectangle.BOTTOM);
			int cellBottomPaddingAddFactor = (isLastCell ? 0 : 2);
			
			PdfPCell timestampCell = new PdfPCell(new Phrase(contentTimestamp + "\n" + contentUser));
			Chunk link = new Chunk(contentHypertext, valueFont);
			link.setAnchor(contentUrl);
			PdfPCell descriptionCell = new PdfPCell(new Phrase(link));
			
			timestampCell.setBorder(cellBorder);
			descriptionCell.setBorder(cellBorder);
			
			timestampCell.setPaddingBottom(timestampCell.getPaddingBottom() + cellBottomPaddingAddFactor);
			descriptionCell.setPaddingBottom(descriptionCell.getPaddingBottom() + cellBottomPaddingAddFactor);
			
			table.addCell(timestampCell);
			table.addCell(descriptionCell);
		};
		
		return table;
		
	}
	
	private PdfPTable writePDFLinksContent(JSONArray jsonContents) throws DocumentException 
	{
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
        
		PdfPTable table = new PdfPTable(new float[] {3,6});
		table.setWidthPercentage(100);
		table.setSpacingBefore(8);
		table.getDefaultCell().setUseAscender(true);
		table.getDefaultCell().setUseDescender(true);
		table.getDefaultCell().setUseBorderPadding(true);
		
		for (int i=0; i<jsonContents.size(); i++)
		{
			JSONObject jsonContent = (JSONObject) jsonContents.get(i);
			String contentHypertext = (String) jsonContent.get("hypertext");
			String contentUrl = (String) jsonContent.get("url");
			
			boolean isLastCell = (i+1 == jsonContents.size());
			int cellBorder = (isLastCell ? Rectangle.NO_BORDER : Rectangle.BOTTOM);
			int cellBottomPaddingAddFactor = (isLastCell ? 0 : 2);
			
			PdfPCell blankCell = new PdfPCell(new Phrase());
			
			Chunk link = new Chunk(contentHypertext, valueFont);
			link.setAnchor(contentUrl);
			PdfPCell descriptionCell = new PdfPCell(new Phrase(link));
			
			blankCell.setBorder(cellBorder);
			descriptionCell.setBorder(cellBorder);
			
			blankCell.setPaddingBottom(blankCell.getPaddingBottom() + cellBottomPaddingAddFactor);
			descriptionCell.setPaddingBottom(descriptionCell.getPaddingBottom() + cellBottomPaddingAddFactor);
			
			table.addCell(blankCell);
			table.addCell(descriptionCell);
		};
		
		return table;
	}
	
	private PdfPTable writePDFNameValueContent(JSONArray jsonContents) throws DocumentException 
	{
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
        
		PdfPTable table = new PdfPTable(new float[] {3,3,3});
		table.setWidthPercentage(100);
		table.setSpacingBefore(8);
		table.getDefaultCell().setUseAscender(true);
		table.getDefaultCell().setUseDescender(true);
		table.getDefaultCell().setUseBorderPadding(true);
		
		for (int i=0; i<jsonContents.size(); i++)
		{
			JSONObject jsonContent = (JSONObject) jsonContents.get(i);
			String contentName = (String) jsonContent.get("name");
			String contentValue = (String) jsonContent.get("value");
			boolean isValueFullWidthCell = 
						jsonContent.containsKey(Styles.PAGE_WIDTH.valueOf()) && 
						PageWidth.getEnumByValue((String) jsonContent.get(Styles.PAGE_WIDTH.valueOf())) == PageWidth.PAGE_WIDTH_FULL;
			
			boolean isLastCell = (i+1 == jsonContents.size());
			int cellBorder = (isLastCell ? Rectangle.NO_BORDER : Rectangle.BOTTOM);
			int cellBottomPaddingAddFactor = (isLastCell ? 0 : 2);
			
			PdfPCell nameCell = new PdfPCell(new Phrase(contentName));
			PdfPCell valueCell = new PdfPCell(new Phrase(contentValue, valueFont));
			PdfPCell blankCell = new PdfPCell();
			
			nameCell.setBorder(cellBorder);
			valueCell.setBorder(cellBorder);
			blankCell.setBorder(isValueFullWidthCell ? cellBorder : Rectangle.NO_BORDER);
			
			nameCell.setPaddingBottom(nameCell.getPaddingBottom() + cellBottomPaddingAddFactor);
			valueCell.setPaddingBottom(nameCell.getPaddingBottom() + cellBottomPaddingAddFactor);
			blankCell.setPaddingBottom(nameCell.getPaddingBottom() + cellBottomPaddingAddFactor);
			
			valueCell.setColspan(isValueFullWidthCell ? table.getNumberOfColumns()-1 : 1);
			
			table.addCell(nameCell);
			table.addCell(valueCell);
			if (!isValueFullWidthCell) table.addCell(blankCell);
		};
		
		return table;
	}
	
	private void createPDF(JSONObject jsonObject, String destFile) throws IOException, DocumentException
	{
		System.out.print("createPDF from JSON...");
		
		Document doc = new Document();
		PdfWriter pdfWriter = PdfWriter.getInstance(doc, new FileOutputStream(destFile));
		doc.open();
		
		JSONArray sections = (JSONArray) jsonObject.get("sections");		
		sections.forEach(jsonSection -> {
			try {
				writePDFSection((JSONObject)jsonSection, doc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		doc.addHeader("header1", "hello world");
		
        doc.close();
        
		System.out.println("done");
	}

}
