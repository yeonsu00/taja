package com.taja.station.application;

import com.taja.global.exception.ReadFileException;
import com.taja.station.domain.Station;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationFileReader {

    private static final String SHEET_NAME = "station state";
    private static final int START_ROW = 5;

    public List<Station> readStationsFromFile(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(SHEET_NAME);

            DataFormatter formatter = new DataFormatter();
            List<Station> stations = new ArrayList<>();

            for (int i = START_ROW; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                try {
                    String latString = formatter.formatCellValue(row.getCell(4));
                    String lonString = formatter.formatCellValue(row.getCell(5));

                    double latitude = latString.isEmpty() ? 0.0 : Double.parseDouble(latString);
                    double longitude = lonString.isEmpty() ? 0.0 : Double.parseDouble(lonString);

                    String numberString = formatter.formatCellValue(row.getCell(0));
                    Integer number = Integer.parseInt(numberString);

                    String lcdString = formatter.formatCellValue(row.getCell(7));
                    String qrString = formatter.formatCellValue(row.getCell(8));

                    Integer lcd = getHoldNumber(lcdString);
                    Integer qr = getHoldNumber(qrString);

                    Station station = Station.builder()
                            .number(number)
                            .name(formatter.formatCellValue(row.getCell(1)))
                            .district(formatter.formatCellValue(row.getCell(2)))
                            .address(formatter.formatCellValue(row.getCell(3)))
                            .latitude(latitude)
                            .longitude(longitude)
                            .lcd(lcd)
                            .qr(qr)
                            .operationMethod(formatter.formatCellValue(row.getCell(9)))
                            .build();

                    stations.add(station);

                } catch (NumberFormatException e) {
                    throw new ReadFileException("엑셀 파일의 데이터 형식이 올바르지 않습니다.");
                }
            }
            workbook.close();

            return stations;

        } catch (IOException | RuntimeException e) {
            log.error(e.getMessage());
            throw new ReadFileException("엑셀 파일 처리 중 오류가 발생했습니다.");
        }
    }

    private Integer getHoldNumber(String holdNumberString) {
        Integer holdNumber = null;

        if (!holdNumberString.isEmpty()) {
            holdNumber = Integer.parseInt(holdNumberString);
        }
        return holdNumber;
    }
}
