package com.zzming.xsapi.util.excel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder;
import org.apache.poi.hssf.eventusermodel.FormatTrackingHSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.eventusermodel.MissingRecordAwareHSSFListener;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.RKRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class XLS2CSV implements HSSFListener {
    private int minColumns;
    private POIFSFileSystem fs;
    private PrintStream output;
    private int lastRowNumber;
    private int lastColumnNumber;
    private boolean outputFormulaValues;
    private EventWorkbookBuilder.SheetRecordCollectingListener workbookBuildingListener;
    private HSSFWorkbook stubWorkbook;
    private SSTRecord sstRecord;
    private FormatTrackingHSSFListener formatListener;
    private int sheetIndex;
    private BoundSheetRecord[] orderedBSRs;
    private ArrayList boundSheetRecords;
    private int nextRow;
    private int nextColumn;
    private boolean outputNextStringRecord;
    private final String OUTPUT_CHARSET = "GBK";

    public XLS2CSV(POIFSFileSystem fs, PrintStream output, int minColumns) {
        this.outputFormulaValues = true;
        this.sheetIndex = -1;
        this.boundSheetRecords = new ArrayList();
        this.fs = fs;
        this.output = output;
        this.minColumns = minColumns;
    }

    public XLS2CSV(String inputFilePath, String outputFilePath) throws Exception {
        this.outputFormulaValues = true;
        this.sheetIndex = -1;
        this.boundSheetRecords = new ArrayList();
        this.fs = new POIFSFileSystem(new FileInputStream(inputFilePath));
        this.output = new PrintStream(outputFilePath, "GBK");
        this.minColumns = -1;
    }

    public XLS2CSV(String filename, int minColumns) throws IOException, FileNotFoundException {
        this(new POIFSFileSystem(new FileInputStream(filename)), System.out, minColumns);
    }

    public void process() throws IOException {
        MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(this);
        this.formatListener = new FormatTrackingHSSFListener(listener);
        HSSFEventFactory factory = new HSSFEventFactory();
        HSSFRequest request = new HSSFRequest();
        if (this.outputFormulaValues) {
            request.addListenerForAllRecords(this.formatListener);
        } else {
            this.workbookBuildingListener = new EventWorkbookBuilder.SheetRecordCollectingListener(this.formatListener);
            request.addListenerForAllRecords(this.workbookBuildingListener);
        }
        factory.processWorkbookEvents(request, this.fs);
    }

    public void processRecord(Record record) {
        RKRecord rkrec;
        NumberRecord numrec;
        NoteRecord nrec;
        LabelSSTRecord lsrec;
        LabelRecord lrec;
        FormulaRecord frec;
        BoolErrRecord berec;
        BlankRecord brec;
        BOFRecord br;
        int thisRow = -1;
        int thisColumn = -1;
        String thisStr = null;
        switch (record.getSid()) {
            case 133:
                this.boundSheetRecords.add(record);
                break;
            case 2057:
                br = (BOFRecord) record;
                if (br.getType() == 16) {
                    if (this.workbookBuildingListener != null && this.stubWorkbook == null) {
                        this.stubWorkbook = this.workbookBuildingListener.getStubHSSFWorkbook();
                    }
                    this.sheetIndex++;
                    if (this.orderedBSRs == null) {
                        this.orderedBSRs = BoundSheetRecord.orderByBofPosition(this.boundSheetRecords);
                    }
                }
                break;
            case 252:
                this.sstRecord = (SSTRecord) record;
                break;
            case 513:
                brec = (BlankRecord) record;
                thisRow = brec.getRow();
                thisColumn = brec.getColumn();
                thisStr = "";
                break;
            case 517:
                berec = (BoolErrRecord) record;
                thisRow = berec.getRow();
                thisColumn = berec.getColumn();
                thisStr = "";
                break;
            case 6:
                frec = (FormulaRecord) record;
                thisRow = frec.getRow();
                thisColumn = frec.getColumn();
                if (this.outputFormulaValues) {
                    if (Double.isNaN(frec.getValue())) {
                        this.outputNextStringRecord = true;
                        this.nextRow = frec.getRow();
                        this.nextColumn = frec.getColumn();
                        break;
                    }
                    thisStr = this.formatListener.formatNumberDateCell(frec);
                    break;
                }
                thisStr = '"' + HSSFFormulaParser.toFormulaString(this.stubWorkbook, frec
                        .getParsedExpression()) + '"';
                break;
            case 519:
                if (this.outputNextStringRecord) {
                    StringRecord srec = (StringRecord) record;
                    thisStr = srec.getString();
                    thisRow = this.nextRow;
                    thisColumn = this.nextColumn;
                    this.outputNextStringRecord = false;
                }
                break;
            case 516:
                lrec = (LabelRecord) record;
                thisRow = lrec.getRow();
                thisColumn = lrec.getColumn();
                thisStr = '"' + lrec.getValue() + '"';
                break;
            case 253:
                lsrec = (LabelSSTRecord) record;
                thisRow = lsrec.getRow();
                thisColumn = lsrec.getColumn();
                if (this.sstRecord == null) {
                    thisStr = "\"(No SST Record, can't identify string)\"";
                    break;
                }
                thisStr = '"' + this.sstRecord.getString(lsrec.getSSTIndex()).toString() + '"';
                break;
            case 28:
                nrec = (NoteRecord) record;
                thisRow = nrec.getRow();
                thisColumn = nrec.getColumn();
                thisStr = "\"(TODO)\"";
                break;
            case 515:
                numrec = (NumberRecord) record;
                thisRow = numrec.getRow();
                thisColumn = numrec.getColumn();
                thisStr = this.formatListener.formatNumberDateCell(numrec);
                break;
            case 638:
                rkrec = (RKRecord) record;
                thisRow = rkrec.getRow();
                thisColumn = rkrec.getColumn();
                thisStr = "\"(TODO)\"";
                break;
        }
        if (thisRow != -1 && thisRow != this.lastRowNumber) {
            this.lastColumnNumber = -1;
        }
        if (record instanceof MissingCellDummyRecord) {
            MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
            thisRow = mc.getRow();
            thisColumn = mc.getColumn();
            thisStr = "";
        }
        if (thisStr != null) {
            if (thisColumn > 0) {
                this.output.print(',');
            }
            this.output.print(thisStr);
        }
        if (thisRow > -1) {
            this.lastRowNumber = thisRow;
        }
        if (thisColumn > -1) {
            this.lastColumnNumber = thisColumn;
        }
        if (record instanceof org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord) {
            if (this.minColumns > 0) {
                if (this.lastColumnNumber == -1) {
                    this.lastColumnNumber = 0;
                }
                for (int i = this.lastColumnNumber; i < this.minColumns; i++) {
                    this.output.print(',');
                }
            }
            this.lastColumnNumber = -1;
            this.output.println();
        }
    }

    public static void main(String[] args) throws Exception {
        XLS2CSV xls2csv = new XLS2CSV("Time.xls", "out.csv");
        xls2csv.process();
    }
}
