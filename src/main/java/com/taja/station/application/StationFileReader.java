package com.taja.station.application;

import com.taja.global.exception.ReadFileException;
import com.taja.station.domain.Station;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
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
                if (row == null) continue;

                try {
                    String latString = formatter.formatCellValue(row.getCell(4));
                    String lonString = formatter.formatCellValue(row.getCell(5));

                    double latitude = latString.isEmpty() ? 0.0 : Double.parseDouble(latString);
                    double longitude = lonString.isEmpty() ? 0.0 : Double.parseDouble(lonString);

                    Station station = Station.builder()
                            .number(formatter.formatCellValue(row.getCell(0)))
                            .name(formatter.formatCellValue(row.getCell(1)))
                            .address(formatter.formatCellValue(row.getCell(3)))
                            .latitude(latitude)
                            .longitude(longitude)
                            .build();

                    stations.add(station);

                } catch (NumberFormatException e) {
                    throw new ReadFileException("엑셀 파일의 데이터 형식이 올바르지 않습니다.");
                }
            }
            workbook.close();

            return stations;

        } catch (IOException | RuntimeException e) {
            throw new ReadFileException("엑셀 파일 처리 중 오류가 발생했습니다.");
        }
    }
}
