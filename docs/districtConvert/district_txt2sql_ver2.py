######### 이 코드는 id를 미리 정하여서 sql문을 생성하기 때문에 서버실행시에 맨 처음 데이터를 주입하여야 한다.
######### 장점: 빠름
######### 단점: id가 정해져 있어 초기실행시 맨 처음 데이터가 들어가야함


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


        city_list=[]
        country_list= []
        district_list= []


        city_sql = "INSERT INTO city_tb (name) \nVALUES \n"
        country_sql = "INSERT INTO country_tb (city_id, name) \nVALUES \n"
        district_sql = "INSERT INTO district_tb (statutory_code, country_id, name) \nVALUES \n"


        # 데이터 삽입 코드 작성
        for index, row in df.iterrows():
            statutory_code = row['법정동코드']
            city_name = row['시도명']
            country_name = row['시군구명']
            district_name = row['읍면동명']

            #시도명 중복처리 및 시도명id 설정
            if city_name not in city_list:
                city_list.append(city_name)
                city_sql += f"('{city_name}'),\n"
            city_id= city_list.index(city_name)+1

            #시군구 중복처리 및 시군구id설정
            if (city_id,country_name) not in country_list:
                country_list.append((city_id,country_name))
                country_sql += f"({city_id}, '{country_name}'),\n"
            country_id = country_list.index((city_id,country_name))+1

            if (country_id,district_name) not in district_list:
                district_list.append((country_id,district_name))
                district_sql += f"({statutory_code}, {country_id}, '{district_name}'),\n"
            district_id = district_list.index((country_id,district_name))+1



        city_sql = city_sql.rstrip(',\n') + ";\n"
        country_sql = country_sql.rstrip(',\n') + ";\n"
        district_sql = district_sql.rstrip(',\n') + ";\n"

        # SQL 문을 파일에 쓰기
        sql_file.write("\n")
        sql_file.write(city_sql)
        sql_file.write("\n")
        sql_file.write(country_sql)
        sql_file.write("\n")
        sql_file.write(district_sql)
        print("SQL 파일 생성완료")



if __name__ == "__main__":
    text_file_path = '법정동코드.txt'
    csv_path = '법정동코드.csv'
    sql_path = '법정동코드.sql'
    # 텍스트 파일을 CSV 파일로 변환
    text_to_csv(text_file_path, csv_path)
    # CSV 파일을 SQL파일로 변환
    csv_to_sql(csv_path,sql_path)

