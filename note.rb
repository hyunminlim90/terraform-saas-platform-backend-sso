=begin




4. [백엔드] ⇢ 플랫폼 오픈을 목표로 하면 작업 속력와 안정성이 개선한다. ⭐







        ✝︎ (그룹/프로젝트)

            📁 opentofu
                ⤷ 📁 opentofu-backend
                    ⤷ 📁 java-sso

        ✝︎ (커맨드)

            git init;
            git config --local user.name "mole";
            git config --local user.email "hyunmin.lim.90@icloud.com";
            git switch --create feature-2025;
            git remote add origin https://gitlab.opentofu.click/opentofu/backend/java-sso.git;
            git add ./; git commit -m "GITLAB 마이그레이션 2026"; git push origin feature-2025;

             ⇢ 마이그레이션

    4.5 데이터베이스 커넥션. ⇢ (note.rb 마킹 값 전체 치환)

        ✝︎ 작업 순서
            (가). 보안.
            (나). 연결 확인.

        ✝︎ 노하우
            (가) 
                (1) 컨테이너.
                
                    (스펙 정의)

                        📁 opentofu-yaml
                            ⤷ 📄 mysql.yaml

                        ✨ 포트 범위는 3307~5283 를 활용한다.

                    (커맨드)

                        docker-compose -f mysql.yaml up -d

                (2) TLS 구성.

                    (마킹 값 치환)

                        sso

                    » (MySQL) ⇢ 마킹 값 전체 치환 ⭐

                        (로컬 정적 호스트 매핑)

                            📁 etc
                                ⤷ 📄 hosts

                            ✝︎ (커맨드)

                                sudo vi /etc/hosts
                            
                            ✝︎ (호스트 매핑)

                                127.0.0.1       sso.private.mysql

                        (인증서)

                            📁 opentofu-cert
                                ⤷ 📁 mysql
                                    ⤷ 📁 sso
                                        ⤷ 📄 ca.crt.pem
                                        ⤷ 📄 sso.private.mysql.crt.pem
                                        ⤷ 📄 sso.private.mysql.key.pem

                            ✝︎ (SAN 지정)

                                📁 opentofu-cert
                                    ⤷ 📁 mysql
                                        ⤷ 📄 endEntity_openssl.cnf

                                ✨ DNS.1 = sso.private.mysql

                            ✝︎ (발급)

                                (마킹 값 치환)

                                    sso

                                (커맨드)

                                    openssl genrsa -out server.key 2048;
                                    openssl req -new -key server.key -out server.csr -config endEntity_openssl.cnf;
                                    openssl x509 -req -in server.csr -CA mysql-intermediateCA.crt -CAkey intermediateCA.key -out server.crt -days 36500 -sha256 -extfile endEntity_openssl.cnf -extensions v3_enduser;
                                    cat ./server.crt ./mysql-intermediateCA.crt ./mysql-rootCA.pem > combined.crt;

                                    mv combined.crt ./sso/ca.crt.pem;
                                    mv server.crt ./sso/sso.private.mysql.crt.pem;
                                    mv server.key ./sso/sso.private.mysql.key.pem;
                                    mv server.csr ./sso/;

                            ✝︎ (인증서 마운트)

                                (마킹 값 치환)

                                    sso

                                (커맨드)

                                    docker cp ca.crt.pem mysql-sso:/etc/pki/tls/certs/;
                                    docker cp sso.private.mysql.crt.pem mysql-sso:/etc/pki/tls/certs/;
                                    docker cp sso.private.mysql.key.pem mysql-sso:/etc/pki/tls/private/;

                            ✝︎ (TLS 구성)

                                (컨테이너 접속)

                                    docker exec -it -u 0 mysql-sso bash

                                » (공개 인증서 저장소)

                                    📁 etc
                                        ⤷ 📁 pki
                                            ⤷ 📁 tls
                                                ⤷ 📁 certs
                                                    ⤷ 📄 ca.crt.pem
                                                    ⤷ 📄 sso.private.mysql.crt.pem
                                    (마킹 값 치환)

                                        sso

                                    (커맨드)

                                        chmod 644 /etc/pki/tls/certs/ca.crt.pem;
                                        chmod 644 /etc/pki/tls/certs/sso.private.mysql.crt.pem;
                                        ls -l /etc/pki/tls/certs/ca.crt.pem;
                                        ls -l /etc/pki/tls/certs/sso.private.mysql.crt.pem;

                                » (개인키 저장소)

                                    📁 etc
                                        ⤷ 📁 pki
                                            ⤷ 📁 tls
                                                ⤷ 📁 private
                                                    ⤷ 📄 sso.private.mysql.key.pem

                                    (마킹 값 치환)

                                        sso

                                    (커맨드)

                                        chmod 640 /etc/pki/tls/private/sso.private.mysql.key.pem;
                                        chgrp mysql /etc/pki/tls/private/sso.private.mysql.key.pem;
                                        ls -l /etc/pki/tls/private/sso.private.mysql.key.pem;

                                » (서버측 파라미터 구성)

                                    📁 etc
                                        ⤷ 📁 my.cnf.d
                                            ⤷ 📄 mysql-server-tls.cnf

                                    (마킹 값 치환)

                                        sso

                                    (커맨드)

                                        cat > /etc/my.cnf.d/mysql-server-tls.cnf

                                        [mysqld]
                                        ssl_key = /etc/pki/tls/private/sso.private.mysql.key.pem
                                        ssl_cert = /etc/pki/tls/certs/sso.private.mysql.crt.pem
                                        ssl_ca = /etc/pki/tls/certs/ca.crt.pem
                                        
                                        require_secure_transport = on
                                        tls_version = TLSv1.2,TLSv1.3
                                        
                                        general_log = 1
                                        general_log_file = /var/log/mysql/general.log

                                        cat /etc/my.cnf.d/mysql-server-tls.cnf

                                » (클라이언트측 파라미터 구성)

                                    📁 etc
                                        ⤷ 📁 my.cnf.d
                                            ⤷ 📄 mysql-client-tls.cnf

                                    cat > /etc/my.cnf.d/mysql-client-tls.cnf

                                    [client]
                                    ssl-mode = VERIFY_IDENTITY
                                    ssl-ca = /etc/pki/tls/certs/ca.crt.pem

                                    cat /etc/my.cnf.d/mysql-client-tls.cnf

                                » (사용자 구성)

                                    (마킹 값 치환)

                                        {{__포트__}}
                                        {{__스네이크케이스_AWS제외__}}
                                        sso

                                    (커맨드) ⇢ MySQL Workbench

                                        CREATE USER 'sysadmin'@'%' IDENTIFIED BY 'Sysadmin1234!!';
                                        GRANT ALL PRIVILEGES ON awscloud_{{__스네이크케이스_AWS제외__}}.* TO 'sysadmin'@'%';
                                        GRANT CREATE USER, ALTER ON *.* TO 'sysadmin'@'%';
                                        GRANT RELOAD ON *.* TO 'sysadmin'@'%';
                                        ALTER USER 'sysadmin'@'%' REQUIRE SSL;
                                        FLUSH PRIVILEGES;
                                        SHOW GRANTS FOR 'sysadmin'@'%';

                                        exit
                                        mysql -usysadmin -p -h 127.0.0.1 -P{{__포트__}}
                                        docker restart mysql-sso
                                        mysql -usysadmin -p -h sso.private.mysql -P{{__포트__}} -e status

                    » (Java)  ⇢ 마킹 값 전체 치환 ⭐

                        (마킹 값 치환)

                            sso

                        (로컬 정적 호스트 매핑)

                            📁 etc
                                ⤷ 📄 hosts

                            ✝︎ (커맨드)

                                sudo vi /etc/hosts
                            
                            ✝︎ (호스트 매핑)

                                127.0.0.1       java-sso.opentofu.click

                        (PKCS#12) ⇢ 서버 신원 보증

                            📁 opentofu-cert
                                ⤷ 📁 java
                                    ⤷ 📁 sso
                                        ⤷ 📄 keystore.p12
                                        ⤷ 📄 server.crt
                                        ⤷ 📄 server.key

                            ✝︎ (SAN 지정)

                                📁 opentofu-cert
                                    ⤷ 📁 java
                                        ⤷ 📄 endEntity_openssl.cnf

                                ✨ DNS.1 = java-sso.opentofu.click

                            ✝︎ (키스토어 저장)

                                (마킹 값 치환)

                                    sso
                                
                                (커맨드)

                                    openssl genrsa -out server.key 2048;
                                    openssl req -new -key server.key -out server.csr -config endEntity_openssl.cnf;
                                    openssl x509 -req -in server.csr -CA java-intermediateCA.crt -CAkey intermediateCA.key -out server.crt -days 36500 -sha256 -extfile endEntity_openssl.cnf -extensions v3_enduser;
                                    cat ./server.crt ./java-intermediateCA.crt ./java-rootCA.pem > combined.crt;

                                    rm -rf server.crt server.csr;
                                    mv combined.crt ./sso/server.crt;
                                    mv server.key ./sso/;

                                    export P12_PASS='{{__PKCS#12_비밀번호__}}';
                                    openssl pkcs12 -export -in server.crt -inkey server.key -out keystore.p12 -name 'java-sso.opentofu.click' -passout env:P12_PASS;

                                ✨ 비밀번호는 ecstaskdefinition1234!! 형식으로 지정한다.

                            ✝︎ (키스토어 설치)

                                📁 src
                                    ⤷ 📁 main
                                        ⤷ 📁 resources
                                            ⤷ 📄 keystore.p12

                        » (PKIX / X.509 Trust Model) ⇢ 서버 신원 검증 [🔗 6.3]

                            📁 opentofu-cert
                                ⤷ 📄 truststore.p12

                            (CA 신뢰 등록)

                                mkdir -p /opt/app/certs/

                                keytool -importcert -alias java-root-ca -file ./java/java-rootCA.pem -keystore truststore.p12 -storetype PKCS12
                                keytool -importcert -alias java-intermediate-ca -file ./java/java-intermediateCA.crt -keystore truststore.p12 -storetype PKCS12
                                keytool -importcert -alias mysql-root-ca -file ./mysql/mysql-rootCA.pem -keystore truststore.p12 -storetype PKCS12
                                keytool -importcert -alias mysql-intermediate-ca -file ./mysql/mysql-intermediateCA.crt -keystore truststore.p12 -storetype PKCS12
                                keytool -importcert -alias react-root-ca -file ./react/react-rootCA.pem -keystore truststore.p12 -storetype PKCS12
                                keytool -importcert -alias react-intermediate-ca -file ./react/react-intermediateCA.crt -keystore truststore.p12 -storetype PKCS12
                                keytool -list -v -keystore truststore.p12

                                mv truststore.p12 /opt/app/certs/

                                nohup java -Djavax.net.ssl.trustStore=/opt/app/certs/truststore.p12 -Djavax.net.ssl.trustStorePassword=changeit -jar "../vpc/target/vpc-0.0.1-SNAPSHOT.jar" > vpc-output.log 2>&1 &

                (3) application.properties

                    (샘플)

                        📁 sample
                            ⤷ 📁 java
                                ⤷ 📄 application.properties

                    (마킹 값 치환)

                        sso
                        {{__스네이크케이스_AWS제외__}}
                        {{__JAVA_포트__}}
                        {{__MYSQL_포트__}}
                        {{__PKCS#12_비밀번호__}}

                    ✨ 포트 범위는 1024~3000 를 활용한다.

            (나)
                (1) 연결 확인.
                
                    (소스 코드 빌드)

                        mvn clean install

                (2) MySQL 루트 사용자 관리.

                    (데이터베이스 접속)

                        mysql -uroot -p'Mysql1234!!' -h 127.0.0.1 -P{{__포트__}}

                    (사용자 확인)

                        SELECT user, host, authentication_string, ssl_type, plugin FROM mysql.user WHERE user='root';

                    (사용자 정리)

                        DROP USER 'root'@'%';

    4.6 소스 코드 작업.

        ✝︎ 작업 순서
            (가). 코드 작업.
            (나). 소스 코드 빌드.
            (다). 프로세스 실행.

        ✝︎ 노하우
            (가) 
                (1) 코드 작업.
                
                    (소스 코드 복제)

                        ✨ ecs task definition 프로젝트 소스 코드를 복제한다. [🔗 6.3]
                        ✨ config 패키지부터 util 패키지까지 복제한다. ⇢ 297개로 확인

                    » (패키지 별 작업)

                        ✨ 모든 클래스는 {{__스네이크케이스_AWS제외__}} 로 치환한다. ⇢ (Command + Shift + F)

                        (마킹 값 치환) ⇢ Command + Shift + F

                            taskId
                            taskArn
                            task_id
                            task_arn

                            ✨ {{__카멜케이스_AWS제외__}} 로 프로젝트 전체에서 치환한다.
                            ✨ {{__스네이크케이스_AWS제외__}} 로 프로젝트 전체에서 치환한다.

                        (constants package)

                            📁 constants
                                ⤷ 📄 AppConstants.class

                            ✨ TEXTAREA_PARAMS는 {{__카멜케이스_파라미터명__}} 또는 List.of() 로 지정한다. ⇢ note.rb 파일에서 text 검색

                        » (dto package)

                            📁 dto
                                ⤷ 📄 ResourceDto.class
                                ⤷ 📄 TofuDto.class

                            (TofuDto class)

                                ✨ version.2 를 활용한다.

                                » (레코드 컴포넌트 타입) ⇢ 타입 기반 전체 치환 ⭐

                                    (string number textarea) ⇢ □ 포함

                                        String 

                                    (boolean) ⇢ □ 포함

                                        Boolean 

                                    (string[] boolean[] number[]) ⇢ ⧉ 포함

                                        List<String> 

                                    (object)

                                        Map<String, String> 

                                    » (object[])

                                        List<Map<String, String>> 

                                        ✝︎ (JavaBeans getter 호출부)

                                            e.get{{__파스칼케이스_파라미터명__}}().stream()
                                                .map({{__중간_엔티티_클래스명__}}::get{{__파스칼케이스_파라미터명__}})
                                                .toList(),

                                    » (<string[]>[])

                                        List<List<String>> 

                                        ✝︎ (JavaBeans getter 호출부)

                                            e.get{{__파스칼케이스_파라미터명__}}().stream()
                                                .map({{__중간_엔티티_클래스명__}}::get{{__파스칼케이스_파라미터명__}})
                                                .toList(),
                                        
                                    (첫 알파벳 대문자)

                                        ✨ 화면 표시와 테라폼 파라미터 입력에는 첫 알파벳 대문자로 사용한다.
                                        ✨ api 호출의 경우에만 정식 네이밍 컨벤션을 사용한다.

                        » (handler package)

                            ✝︎ (abstracts package)

                                📁 handler
                                    ⤷ 📁 entity
                                        ⤷ 📁 abstracts
                                            ⤷ 📄 BaseEntity.class
                                    
                                ✨ 파라미터 version.2 를 활용한다.

                                » (파라미터 타입 별 필드 타입) ⇢ 타입 기반 전체 치환 ⭐
                                
                                    (string number) ⇢ □ 포함

                                        private String 

                                    (textarea)

                                        @Lob
                                        @Column(columnDefinition = "LONGTEXT")
                                        private String 

                                    (boolean) ⇢ □ 포함

                                        private Boolean 

                                    (string[] boolean[] number[]) ⇢ ⧉ 포함

                                        @ElementCollection(fetch = FetchType.EAGER)
                                        private List<String> 

                                    (object object[] <string[]>[])

                                        ✨ TofuEntity.class에 선언한다.

                                    (첫 알파벳 대문자)

                                        ✨ 화면 표시와 테라폼 파라미터 입력에는 첫 알파벳 대문자로 사용한다.
                                        ✨ api 호출의 경우에만 정식 네이밍 컨벤션을 사용한다.

                            ✝︎ (entity package)

                                » (중간 엔티티 샘플)

                                    📁 sample
                                        ⤷ 📁 java
                                            ⤷ 📄 JoinListEntityGroup.class [🔗 6.3]
                                            ⤷ 📄 JoinMapEntityGroup.class [🔗 6.3]

                                    (마킹 값 치환)

                                        {{__스네이크케이스_AWS제외__}}
                                        {{__스네이크케이스_파라미터명_단수__}}
                                        {{__스네이크케이스_파라미터명_복수__}}
                                        {{__파스칼케이스_파라미터명_단수__}}
                                        {{__확장_속성명__}}

                                    ✨ object[] 와 <string[]>[] 는 중간 엔티티 클래스를 활용한다.
                                    ✨ 확장 속성명은 {{__카멜케이스_파라미터명__}} 지정한다.

                                📁 handler
                                    ⤷ 📁 entity
                                        ⤷ 📁 entity
                                            ⤷ 📄 TofuEntity.class
                
                                » (파라미터 타입 별 필드 스펙)

                                    (object)

                                        ✝︎ (마킹 값 치환)

                                            {{__스네이크케이스_AWS제외__}}
                                            {{__스네이크케이스_파라미터명_단수__}}
                                            {{__스네이크케이스_파라미터명_복수__}}
                                            {{__카멜케이스_파라미터명__}}

                                        ✝︎ (필드 스펙)

                                            @ElementCollection(fetch = FetchType.EAGER)
                                            @CollectionTable(name = "{{__스네이크케이스_AWS제외__}}_entity_{{__스네이크케이스_파라미터명_복수__}}", joinColumns = @JoinColumn(name = "{{__스네이크케이스_AWS제외__}}_entity_pk"))
                                            @MapKeyColumn(name = "{{__스네이크케이스_파라미터명_단수__}}_key")
                                            @Column(name = "{{__스네이크케이스_파라미터명_단수__}}_value")
                                            @Builder.Default
                                            private Map<String, String> {{__카멜케이스_파라미터명__}} = new HashMap<>();

                                    (object[])

                                        ✝︎ (마킹 값 치환)

                                            {{__파스칼케이스_파라미터명_단수__}}
                                            {{__파스칼케이스_파라미터명_복수__}}
                                            {{__카멜케이스_파라미터명_단수__}}
                                            {{__카멜케이스_파라미터명_복수__}}
                                            {{__파스칼케이스_확장_속성명__}}

                                        ✝︎ (필드 스펙)

                                            @OneToMany(mappedBy = "tofuEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
                                            @Builder.Default
                                            @BatchSize(size = 50)
                                            private List<{{__파스칼케이스_파라미터명_단수__}}Group> {{__카멜케이스_파라미터명_복수__}} = new ArrayList<>();

                                            @JsonGetter("{{__카멜케이스_파라미터명_복수__}}")
                                            public List<Map<String, String>> get{{__파스칼케이스_파라미터명_복수__}}AsMap() {
                                                return this.{{__카멜케이스_파라미터명_복수__}}.stream()
                                                    .map(({{__카멜케이스_파라미터명_단수__}}Group) -> {{__카멜케이스_파라미터명_단수__}}Group.get{{__파스칼케이스_확장_속성명__}}())
                                                    .collect(Collectors.toList());
                                            }

                                    (<string[]>[])

                                        ✝︎ (마킹 값 치환)

                                            {{__카멜케이스_파라미터명_복수__}}
                                            {{__카멜케이스_파라미터명_단수__}}
                                            {{__파스칼케이스_파라미터명_복수__}}
                                            {{__파스칼케이스_파라미터명_단수__}}
                                            {{__파스칼케이스_확장_속성명__}}

                                        ✝︎ (필드 스펙)

                                            @OneToMany(mappedBy = "tofuEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
                                            @Builder.Default
                                            @BatchSize(size = 50)
                                            private List<{{__파스칼케이스_파라미터명_단수__}}Group> {{__카멜케이스_파라미터명_복수__}} = new ArrayList<>();

                                            @JsonGetter("{{__카멜케이스_파라미터명_복수__}}")
                                            public List<List<String>> get{{__파스칼케이스_파라미터명_복수__}}AsList() {
                                                return this.{{__카멜케이스_파라미터명_복수__}}.stream()
                                                    .map(({{__카멜케이스_파라미터명_단수__}}Group) -> {{__카멜케이스_파라미터명_단수__}}Group.get{{__파스칼케이스_확장_속성명__}}())
                                                    .collect(Collectors.toList());
                                            }

                            ✝︎ (implementations package) ⇢ entity

                                📁 handler
                                    ⤷ 📁 entity
                                        ⤷ 📁 implementations
                                            ⤷ 📄 DuplicateEntityBuilder.class
                                            ⤷ 📄 EntityBuilder.class
                                    
                                ✨ 파라미터 version.2 를 활용한다.

                                » (DTO 엔티티 매핑) ⇢ 타입 기반 전체 치환 ⭐

                                    (string number textarea) ⇢ □ 포함

                                        (parameters.path("{{__JSON_필드__}}").asText())

                                    (boolean) ⇢ □ 포함

                                        (parameters.path("{{__JSON_필드__}}").asBoolean())

                                    (object)

                                        (parse{{__파스칼케이스_파라미터명__}}(parameters.path("{{__JSON_필드__}}"))) 

                                        ✨ 컨버터 메서드를 선언한다. [🔗 6.3]

                                    (string[] number[] boolean[]) ⇢ ⧉ 포함

                                        (parseToListString(parameters.path("{{__JSON_필드__}}")))
                                    
                                    (object[])

                                        ✝︎ (마킹 값 치환)

                                            {{__파스칼케이스_파라미터명_복수__}}
                                            {{__파스칼케이스_파라미터명_단수__}}
                                            {{__카멜케이스_파라미터명_복수__}}
                                            {{__JSON_필드__}}

                                        ✝︎ (필드 스펙)

                                            List<{{__파스칼케이스_파라미터명_단수__}}Group> {{__카멜케이스_파라미터명_복수__}} = parseTo{{__파스칼케이스_파라미터명_단수__}}GroupList(parameters.path("{{__JSON_필드__}}"), tofuEntity);
                                            tofuEntity.set{{__파스칼케이스_파라미터명_복수__}}({{__카멜케이스_파라미터명_복수__}});

                                        ✨ build() 아래에 선언한다.
                                        ✨ 컨버터 메서드를 선언한다. [🔗 6.3]

                                    (<string[]>[])

                                        ✝︎ (마킹 값 치환)

                                            {{__파스칼케이스_파라미터명_복수__}}
                                            {{__파스칼케이스_파라미터명_단수__}}
                                            {{__카멜케이스_파라미터명_복수__}}
                                            {{__JSON_필드__}}

                                        ✝︎ (필드 스펙)

                                            List<{{__파스칼케이스_파라미터명_단수__}}Group> {{__카멜케이스_파라미터명_복수__}} = parseTo{{__파스칼케이스_파라미터명_단수__}}GroupList(parameters.path("{{__JSON_필드__}}"), tofuEntity);
                                            tofuEntity.set{{__파스칼케이스_파라미터명_복수__}}({{__카멜케이스_파라미터명_복수__}});

                                        ✨ build() 아래에 선언한다.
                                        ✨ 컨버터 메서드를 선언한다. [🔗 6.3]

                            ✝︎ (implementations package) ⇢ python

                                📁 handler
                                    ⤷ 📁 python
                                        ⤷ 📁 implementations
                                            ⤷ 📄 AwsEcsTaskDefinitionBoto.class

                                ✨ 리전에 하드코딩을 수정한다.

                            ✝︎ (implementations package) ⇢ tofu_module

                                📁 handler
                                    ⤷ 📁 tofu
                                        ⤷ 📁 module
                                            ⤷ 📁 implementations
                                                ⤷ 📄 TofuModule.class

                                ✨ 'index_key'에서 unique value를 지정한다.
                                ✨ yaml hcl 변환부를 지정한다.

                                » (Yaml 필드 매핑) ⇢ 타입 종류대로 지정 속력 개선 ⭐

                                    (name)

                                        ✨ name 필드는 index_key와 동일한 엔티티로 인식하는데 사용한다.
                                        
                                    (string boolean number)

                                        ✝︎ (마킹 값 치환)

                                            {{__스네이크케이스_파라미터명__}}

                                        ✝︎ (매핑 코드)

                                            definedPythonFile
                                                .append("    {{__스네이크케이스_파라미터명__}}:\n");
                                                    for (Map<String, Object> resourceConfig : resourceConfigs) {
                                                        try {
                                                            definedPythonFile
                                                                .append("        - \"" + resourceConfig.get("{{__스네이크케이스_파라미터명__}}") + "\"\n");
                                                        } catch (Exception error) {
                                                            throw new RuntimeException("build_tofu_module_config_file_{{__스네이크케이스_파라미터명__}}");
                                                        }
                                                    }

                                    » (textarea)

                                        ✝︎ (shell script)

                                            (마킹 값 치환)

                                                {{__스네이크케이스_파라미터명__}}
                                                {{__카멜케이스_파라미터명__}}
                                                {{__카멜케이스_AWS제외__}}
                                                {{__스네이크케이스_AWS제외__}}

                                            (매핑 코드)

                                                definedPythonFile
                                                    .append("    {{__스네이크케이스_파라미터명__}}:\n");
                                                        for (Map<String, Object> resourceConfig : resourceConfigs) {
                                                            try {
                                                                String {{__카멜케이스_파라미터명__}} = (String) resourceConfig.get("{{__스네이크케이스_파라미터명__}}");
                                                                if ({{__카멜케이스_파라미터명__}} == null || {{__카멜케이스_파라미터명__}}.isEmpty()) {
                                                                    definedPythonFile
                                                                        .append("    - \"\"\n");
                                                                } else {
                                                                    String {{__카멜케이스_AWS제외__}}Id = (String) resourceConfig.get("{{__스네이크케이스_AWS제외__}}_id");
                                                                    if (!{{__카멜케이스_AWS제외__}}Id.contains("create-only-")) {
                                                                        if ({{__카멜케이스_AWS제외__}}Id.startsWith("arn:")) {
                                                                            String resource = {{__카멜케이스_AWS제외__}}Id.split(":", 6)[5];
                                                                            String[] tokens = resource.split("[/:]");
                                                                            {{__카멜케이스_AWS제외__}}Id = String.join("-", tokens);
                                                                        }
                                                                    }
                                                                    definedPythonFile
                                                                        .append("    - \"" + {{__카멜케이스_AWS제외__}}Id + "_{{__카멜케이스_파라미터명__}}.sh\"\n");
                                                                }
                                                            } catch (Exception error) {
                                                                throw new RuntimeException("build_tofu_module_config_file_{{__스네이크케이스_파라미터명__}}");
                                                            }
                                                        }

                                        ✝︎ (Json)

                                            (마킹 값 치환)

                                                {{__스네이크케이스_파라미터명__}}
                                                {{__스네이크케이스_AWS제외__}}
                                                {{__카멜케이스_파라미터명__}}
                                                {{__카멜케이스_AWS제외__}}

                                            (매핑 코드)

                                                definedPythonFile
                                                    .append("    {{__스네이크케이스_파라미터명__}}:\n");
                                                        for (Map<String, Object> resourceConfig : resourceConfigs) {
                                                            try {
                                                                boolean isEmpty = false;
                                                                Object value = resourceConfig.get("{{__스네이크케이스_파라미터명__}}");
                                                                
                                                                if (value == null) { isEmpty = true; }
                                                                
                                                                else if (value instanceof String) {
                                                                    ObjectMapper mapper = new ObjectMapper();
                                                                    JsonNode node = mapper.readTree((String) value);
                                                                    if ((node.isArray() || node.isObject()) && node.size() == 0) { isEmpty = true; }
                                                                
                                                                } else if (value instanceof List) {
                                                                    if (((List<?>) value).isEmpty()) { isEmpty = true; }
                                                                
                                                                } else if (value instanceof Map) {
                                                                    if (((Map<?, ?>) value).isEmpty()) { isEmpty = true; }
                                                                }
                                                                    
                                                                if (isEmpty) {
                                                                    definedPythonFile
                                                                        .append("        - \"\"\n");
                                                                } else {
                                                                    String {{__카멜케이스_AWS제외__}}Id = (String) resourceConfig.get("{{__스네이크케이스_AWS제외__}}_id");
                                                                    if (!{{__카멜케이스_AWS제외__}}Id.contains("create-only-")) {
                                                                        if ({{__카멜케이스_AWS제외__}}Id.startsWith("arn:")) {
                                                                            String resource = {{__카멜케이스_AWS제외__}}Id.split(":", 6)[5];
                                                                            String[] tokens = resource.split("[/:]");
                                                                            {{__카멜케이스_AWS제외__}}Id = String.join("-", tokens);
                                                                        }
                                                                    }
                                                                    definedPythonFile
                                                                        .append("        - \"" + {{__카멜케이스_AWS제외__}}Id + "_{{__카멜케이스_파라미터명__}}.json\"\n");
                                                                }
                                                            } catch (Exception error) {
                                                                throw new RuntimeException("build_tofu_module_config_file_{{__스네이크케이스_파라미터명__}}");
                                                            }
                                                        }

                                    (string[] boolean[] number[])

                                        ✝︎ (마킹 값 치환)

                                            {{__스네이크케이스_파라미터명__}}
                                            {{__카멜케이스_파라미터명__}}

                                        ✝︎ (매핑 코드)

                                            definedPythonFile
                                                .append("    {{__스네이크케이스_파라미터명__}}:\n");
                                                    for (Map<String, Object> resourceConfig : resourceConfigs) {
                                                        try {
                                                            @SuppressWarnings("unchecked")
                                                            List<Object> {{__카멜케이스_파라미터명__}} = (List<Object>) resourceConfig.get("{{__스네이크케이스_파라미터명__}}");
                                                            definedPythonFile
                                                                .append("        - \"" + String.join("{{__,__}}", {{__카멜케이스_파라미터명__}}.stream().map(Object::toString).collect(Collectors.toList())) + "\"\n");
                                                        } catch (Exception error) {
                                                            throw new RuntimeException("build_tofu_module_config_file_{{__스네이크케이스_파라미터명__}}");
                                                        }
                                                    }

                                    (object)

                                        ✝︎ (마킹 값 치환)

                                            {{__스네이크케이스_파라미터명__}}
                                            {{__카멜케이스_파라미터명__}}
                                            {{__카멜케이스_파라미터명_단수__}}
                                            {{__파스칼케이스_단수_띄어쓰기__}}

                                        ✝︎ (매핑 코드)

                                            definedPythonFile
                                                .append("    {{__스네이크케이스_파라미터명__}}:\n");
                                                    for (Map<String, Object> resourceConfig : resourceConfigs) {
                                                        try {
                                                            @SuppressWarnings("unchecked")
                                                            Map<String, Object> {{__카멜케이스_파라미터명__}} = (Map<String, Object>) resourceConfig.get("{{__스네이크케이스_파라미터명__}}");
                                                            if ({{__카멜케이스_파라미터명__}} != null && !{{__카멜케이스_파라미터명__}}.isEmpty()) {
                                                                String {{__카멜케이스_파라미터명_단수__}}Result = {{__카멜케이스_파라미터명__}}.entrySet().stream() 
                                                                    .map((entry) -> { return entry.getKey() + "{{__:__}}" + entry.getValue(); })
                                                                    .collect(Collectors.joining("{{__,__}}"));
                                                                definedPythonFile.append("        - \"" + {{__카멜케이스_파라미터명_단수__}}Result + "\"\n");
                                                            } else {
                                                                definedPythonFile
                                                                    .append("        - \"\"\n");
                                                                log.warn("------------------------------------------------------------------------------------------------------------------------------------");
                                                                log.warn("{{__파스칼케이스_단수_띄어쓰기__}} does not exist, so it was processed as ''. The location of the class is handler/tofu/module/implementations/TofuModule.java");
                                                                log.warn("------------------------------------------------------------------------------------------------------------------------------------");  
                                                            }
                                                        } catch (Exception error) {
                                                            throw new RuntimeException("build_tofu_module_config_file_{{__스네이크케이스_파라미터명__}}");
                                                        }
                                                    }

                                    (object[])

                                        ✝︎ (마킹 값 치환)

                                            {{__스네이크케이스_파라미터명__}}
                                            {{__카멜케이스_파라미터명__}}
                                            {{__카멜케이스_파라미터명_단수__}}
                                            {{__파스칼케이스_단수_띄어쓰기__}}

                                        ✝︎ (매핑 코드)

                                            definedPythonFile
                                                .append("    {{__스네이크케이스_파라미터명__}}:\n");
                                                    for (Map<String, Object> resourceConfig : resourceConfigs) {
                                                        try {
                                                            @SuppressWarnings("unchecked")
                                                            List<Map<String, Object>> {{__카멜케이스_파라미터명__}} = (List<Map<String, Object>>) resourceConfig.get("{{__스네이크케이스_파라미터명__}}");
                                                            if ({{__카멜케이스_파라미터명__}} != null && !{{__카멜케이스_파라미터명__}}.isEmpty()) {
                                                                List<String> {{__카멜케이스_파라미터명_단수__}}Strings = new ArrayList<>();
                                                                for (Map<String, Object> item : {{__카멜케이스_파라미터명__}}) {
                                                                    List<String> keyValuePairs = new ArrayList<>();
                                                                    for (Map.Entry<String, Object> entry : item.entrySet()) {
                                                                        keyValuePairs.add(entry.getKey() + "{{__:__}}" + entry.getValue());
                                                                    }
                                                                    {{__카멜케이스_파라미터명_단수__}}Strings.add(String.join("{{__,__}}", keyValuePairs));
                                                                }
                                                                definedPythonFile
                                                                    .append("        - \"")
                                                                    .append(String.join("{{__/__}}", {{__카멜케이스_파라미터명_단수__}}Strings))
                                                                    .append("\"\n"); 
                                                            } else {
                                                                definedPythonFile
                                                                    .append("        - \"\"\n");
                                                                log.warn("------------------------------------------------------------------------------------------------------------------------------------");
                                                                log.warn("{{__파스칼케이스_단수_띄어쓰기__}} does not exist, so it was processed as ''. The location of the class is handler/tofu/module/implementations/TofuModule.java");
                                                                log.warn("------------------------------------------------------------------------------------------------------------------------------------");  
                                                            }
                                                        } catch (Exception error) {
                                                            throw new RuntimeException("build_tofu_module_config_file_{{__스네이크케이스_파라미터명__}}");
                                                        }
                                                    }

                                    (<string[]>[])

                                        ✝︎ (마킹 값 치환)

                                            {{__스네이크케이스_파라미터명__}}
                                            {{__카멜케이스_파라미터명__}}
                                            {{__카멜케이스_파라미터명_단수__}}
                                            {{__파스칼케이스_단수_띄어쓰기__}}

                                        ✝︎ (매핑 코드)

                                            definedPythonFile
                                                .append("    {{__스네이크케이스_파라미터명__}}:\n");
                                                    for (Map<String, Object> resourceConfig : resourceConfigs) {
                                                        try {
                                                            @SuppressWarnings("unchecked")
                                                            List<List<String>> {{__카멜케이스_파라미터명__}} = (List<List<String>>) resourceConfig.get("{{__스네이크케이스_파라미터명__}}");
                                                            if ({{__카멜케이스_파라미터명__}} != null && !{{__카멜케이스_파라미터명__}}.isEmpty()) {
                                                                List<String> {{__카멜케이스_파라미터명_단수__}}Strings = new ArrayList<>();
                                                                for (List<String> item : {{__카멜케이스_파라미터명__}}) {
                                                                    String joinedInner = String.join("{{__,__}}", item);
                                                                    {{__카멜케이스_파라미터명_단수__}}Strings.add(joinedInner);
                                                                }
                                                                definedPythonFile
                                                                    .append("        - \"")
                                                                    .append(String.join("{{__/__}}", {{__카멜케이스_파라미터명_단수__}}Strings))
                                                                    .append("\"\n"); 
                                                            } else {
                                                                definedPythonFile
                                                                    .append("        - \"\"\n");
                                                                log.warn("------------------------------------------------------------------------------------------------------------------------------------");
                                                                log.warn("{{__파스칼케이스_단수_띄어쓰기__}} does not exist, so it was processed as ''. The location of the class is handler/tofu/module/implementations/TofuModule.java");
                                                                log.warn("------------------------------------------------------------------------------------------------------------------------------------");  
                                                            }
                                                        } catch (Exception error) {
                                                            throw new RuntimeException("build_tofu_module_config_file_{{__스네이크케이스_파라미터명__}}");
                                                        }
                                                    }

                                ✨ tags 파라미터를 사용하는지 확인한다.
                                ✨ yaml 리프 노드를 지정한다.

                            ✝︎ (implementations package) ⇢ tofu_resource

                                📁 handler
                                    ⤷ 📁 tofu
                                        ⤷ 📁 resource
                                            ⤷ 📁 implementations
                                                ⤷ 📄 TofuResource.class

                                (backslash 치환)

                                    📁 terraform_resource
                                        ⤷ 📁 module_name
                                            ⤷ 📄 main.tf

                                ✨ main.tf 파일을 main.backslash.txt 파일로 복제하고, " 를 \" 로 치환한다.
                                ✨ tags 파라미터를 사용하는지 확인한다.
                                ✨ lifecycle ignore_changes 를 확인한다.

                        » (service package)

                            ✝︎ (implementations package)

                                📁 service
                                    ⤷ 📁 implementations
                                        ⤷ 📄 CleanUpDirectory.class
                                        ⤷ 📄 DeleteDraftVersion.class
                                        ⤷ 📄 DuplicateDraftVersions.class
                                        ⤷ 📄 EntitySaveResource.class
                                        ⤷ 📄 ExecTofuApply.class
                                        ⤷ 📄 ExecTofuDestroy.class
                                        ⤷ 📄 ExecTofuPlan.class
                                        ⤷ 📄 ExecTofuPlanDestroy.class
                                        ⤷ 📄 ExistsDraftVersion.class
                                        ⤷ 📄 ExistsResourceSaveName.class
                                        ⤷ 📄 IssueUniqueId.class
                                        ⤷ 📄 JsonNodeLoadResource.class
                                        ⤷ 📄 LoadDraftVersion.class
                                        ⤷ 📄 PythonLoadResource.class
                                        ⤷ 📄 TofuModule.class
                                        ⤷ 📄 TofuResource.class

                                » (TofuModule class) 📌 textarea 타입의 경우 해당

                                    (파일명 정규화)

                                        ✝︎ (주석)

                                            /** File extension varies by textarea parameter */

                                    ✨ 조건 분기에 {{__카멜케이스_파라미터명__}} 을 지정한다.
                                    ✨ 조건 분기에서 파일 확장자를 지정한다.

                        » (servlet package)

                            ✝︎ (filter package)

                                📁 servlet
                                    ⤷ 📁 filter
                                        ⤷ 📄 CustomCsrfFilter.class
                                        ⤷ 📄 DomainFilter.class
                                        ⤷ 📄 JwtAccessTokenFilter.class
                                
                                (DomainFilter class)

                                    ✨ ALLOWED_HOST 멤버 변수를 application.properties를 활용하여 수정한다.

                        (util package)

                            📁 util
                                ⤷ 📄 AutoCloseUtils.class
                                ⤷ 📄 EmitterUtils.class
                                ⤷ 📄 ExceptionUtils.class
                                ⤷ 📄 FunctionUtils.class
                                ⤷ 📄 GeneralUtils.class
                                ⤷ 📄 ProcessUtils.class
                                ⤷ 📄 TransactionalUtils.class

                            (TransactionalUtils class)

                                ✨ get{{__파스칼케이스_파라미터명__}} 지정하여 LAZY 로딩을 강제 초기화한다.

            (나) 
                (1) 커맨드.

                    ✨ mvn clean install

            (다) 
                (1) 프로세스 실행 커맨드.

                    📁 opentofu-backend
                        ⤷ 📁 nohup-logs
                
                    ✨ nohup java -jar "../sso/target/sso-0.0.1-SNAPSHOT.jar" > sso-output.log 2>&1 &

                (2) 프로세스 포트 조회 커맨드.

                    ✨ lsof -i :1443







=end
