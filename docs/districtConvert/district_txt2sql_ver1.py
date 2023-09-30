#### 이 코드는 중복처리와 id조회를 디비에서 진행하기 때문에 디비에 데이터가 들어있어도 사용할 수 있다. 하지만 디비에서 중복조회를 하고 쿼리가 각각 들어가 디비에 무리가 갈 수 있다.
###### 장점: id가 동적으로 정해진다. 디비에 데이터가 있어도 적용가능하다.
###### 단점: 디비에 비교적 무리가 갈 수 있다. 느리다.


import pandas as pd

# 텍스트 파일을 CSV 파일로 변환
def text_to_csv(text_file_path, csv_path):
    # 텍스트 파일을 데이터프레임으로 읽기
    df = pd.read_csv(text_file_path, sep='\t', names=["법정동코드", "법정동명", "폐지여부"])

    # 폐지여부가 "폐지"인 행은 삭제
    df = df[df['폐지여부'] != '폐지']

    # 법정동명을 띄어쓰기 기준으로 나누어 시도, 시군구, 읍명동으로 분할
    split_result = df['법정동명'].str.split(n=2, expand=True)
    df['시도명'] = split_result[0]
    df['시군구명'] = split_result[1]
    df['읍면동명'] = split_result[2].str.split().str[0]  # 읍면동의 앞부분만 선택

    # null 값이 들어가 있는 행 삭제
    df.dropna(inplace=True)

    # '시도명', '시군구명', '읍면동명'이 동일한 행 중에서 맨 위의 행만 남김
    df = df.drop_duplicates(subset=['시도명', '시군구명', '읍면동명'], keep='first')

    # 결과 데이터프레임을 CSV 파일로 저장
    df.to_csv(csv_path, index=False, encoding='utf-8')
    print("CSV 파일 생성완료")
def csv_to_sql(csv_path, sql_path):
    # CSV 파일 읽기
    df = pd.read_csv('법정동코드.csv')

    # SQL 문을 저장할 파일 열기
    with open('법정동코드.sql', 'w') as sql_file:
        # city_tb 테이블 생성
        sql_statement = """
        CREATE TABLE IF NOT EXISTS city_tb (
            id INT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(255) UNIQUE NOT NULL
        );
        """
        sql_file.write(sql_statement)

        # country_tb 테이블 생성
        sql_statement = """
        CREATE TABLE IF NOT EXISTS country_tb (
            id INT AUTO_INCREMENT PRIMARY KEY,
            city_id INT,
            name VARCHAR(255) NOT NULL,
            FOREIGN KEY (city_id) REFERENCES city_tb(id),
            CONSTRAINT unique_city_name UNIQUE (city_id, name) 
        );
        """
        sql_file.write(sql_statement)

        # district_tb 테이블 생성
        sql_statement = """
        CREATE TABLE IF NOT EXISTS district_tb (
            id INT AUTO_INCREMENT PRIMARY KEY,
            statutory_code BIGINT UNIQUE NOT NULL,
            country_id INT,
            name VARCHAR(255) NOT NULL,
            FOREIGN KEY (country_id) REFERENCES country_tb(id),
            CONSTRAINT unique_country_name UNIQUE (country_id, name)
        );
        """
        sql_file.write(sql_statement)


        # 데이터 삽입 코드 작성
        for index, row in df.iterrows():
            # 시도 테이블에 데이터 삽입 (중복 무시)
            sql_statement = f"INSERT INTO city_tb (name) SELECT '{row['시도명']}' WHERE NOT EXISTS (SELECT 1 FROM city_tb WHERE name = '{row['시도명']}');\n"

            # 시도명에 해당하는 id 가져오기
            sql_statement += f"SET @city_id = (SELECT id FROM city_tb WHERE name = '{row['시도명']}');\n"

            # 시군구 테이블에 데이터 삽입 (중복 확인)
            sql_statement += f"INSERT INTO country_tb (city_id, name) SELECT @city_id, '{row['시군구명']}' WHERE NOT EXISTS (SELECT 1 FROM country_tb WHERE city_id = @city_id AND name = '{row['시군구명']}');\n"

            # 읍면동 테이블에 데이터 삽입 (중복 확인)
            sql_statement += f"INSERT INTO district_tb (statutory_code, country_id, name) SELECT {row['법정동코드']}, (SELECT id FROM country_tb WHERE city_id = @city_id AND name = '{row['시군구명']}'), '{row['읍면동명']}' WHERE NOT EXISTS (SELECT 1 FROM district_tb WHERE  country_id = (SELECT id FROM country_tb WHERE city_id = @city_id AND name = '{row['시군구명']}') AND name = '{row['읍면동명']}');\n"

            # SQL 문을 파일에 쓰기
            sql_file.write(sql_statement)
        print("SQL 파일 생성완료")



if __name__ == "__main__":
    text_file_path = '법정동코드.txt'
    csv_path = '법정동코드.csv'
    sql_path = '법정동코드.sql'
    # 텍스트 파일을 CSV 파일로 변환
    text_to_csv(text_file_path, csv_path)
    # CSV 파일을 SQL파일로 변환
    csv_to_sql(csv_path,sql_path)

