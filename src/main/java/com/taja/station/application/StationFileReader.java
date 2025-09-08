package com.taja.station.application;

import com.taja.global.exception.ReadFileException;
import com.taja.station.domain.Station;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        try (Workbook workbook = openWorkbook(file)) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            DataFormatter formatter = new DataFormatter();

            List<Station> stations = new ArrayList<>();

            for (int i = START_ROW; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                Station station = createStationFromRow(row, formatter);

                if (station != null) {
                    stations.add(station);
                }
            }

            return stations;

        } catch (IOException e) {
            log.error("파일을 읽는 중 I/O 오류가 발생했습니다.", e);
            throw new ReadFileException("파일을 읽는 중 오류가 발생했습니다.");
        } catch (RuntimeException e) {
            log.error("엑셀 파일 처리 중 런타임 오류가 발생했습니다.", e);
            throw new ReadFileException("엑셀 파일 처리 중 오류가 발생했습니다.");
        }
    }

    private Workbook openWorkbook(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        return new XSSFWorkbook(inputStream);
    }

    private Station createStationFromRow(Row row, DataFormatter formatter) {
        try {
            Integer number = Integer.parseInt(formatter.formatCellValue(row.getCell(0)));
            String name = formatter.formatCellValue(row.getCell(1)).trim();
            String district = formatter.formatCellValue(row.getCell(2)).trim();
            String address = formatter.formatCellValue(row.getCell(3)).trim();

            String latString = formatter.formatCellValue(row.getCell(4));
            double latitude = latString.isEmpty() ? 0.0 : Double.parseDouble(latString);

            String lonString = formatter.formatCellValue(row.getCell(5));
            double longitude = lonString.isEmpty() ? 0.0 : Double.parseDouble(lonString);

            Integer lcdHoldCount = getHoldNumber(formatter.formatCellValue(row.getCell(7)));
            Integer qrHoldCount = getHoldNumber(formatter.formatCellValue(row.getCell(8)));
            String operationMethod = formatter.formatCellValue(row.getCell(9)).trim();

            Integer totalHoldCount = calculateTotalHoldCount(lcdHoldCount, qrHoldCount);

            return Station.builder()
                    .number(number)
                    .name(name)
                    .district(district)
                    .address(address)
                    .latitude(latitude)
                    .longitude(longitude)
                    .lcdHoldCount(lcdHoldCount)
                    .qrHoldCount(qrHoldCount)
                    .totalHoldCount(totalHoldCount)
                    .operationMethod(operationMethod)
                    .build();
        } catch (NumberFormatException e) {
            throw new ReadFileException("엑셀 파일의 데이터 형식이 올바르지 않습니다.");
        }
    }

    private Integer getHoldNumber(String holdNumberString) {
        Integer holdNumber = null;

        if (!holdNumberString.isEmpty()) {
            holdNumber = Integer.parseInt(holdNumberString);
        }
        return holdNumber;
    }

    private Integer calculateTotalHoldCount(Integer lcdHoldCount, Integer qrHoldCount) {
        int lcdCount = Optional.ofNullable(lcdHoldCount).orElse(0);
        int qrCount = Optional.ofNullable(qrHoldCount).orElse(0);
        return lcdCount + qrCount;
    }
}
