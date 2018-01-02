package com.jkkc.travel.bean;

import java.util.List;

/**
 * Created by Guan on 2017/7/12.
 */
public class HeWeather5Bean {


    /**
     * HeWeather5 : [{"now":{"hum":"82","vis":"10","pres":"1008","pcpn":"0.7","fl":"37","tmp":"29","cond":{"txt":"阵雨","code":"300"},"wind":{"sc":"微风","spd":"7","deg":"220","dir":"东南风"}},"basic":{"city":"深圳","update":{"loc":"2017-07-14 12:50","utc":"2017-07-14 04:50"},"lon":"114.08594513","id":"CN101280601","cnty":"中国","lat":"22.54700089"},"status":"ok"}]
     */
    private List<HeWeather5Entity> HeWeather5;

    public void setHeWeather5(List<HeWeather5Entity> HeWeather5) {
        this.HeWeather5 = HeWeather5;
    }

    public List<HeWeather5Entity> getHeWeather5() {
        return HeWeather5;
    }

    public class HeWeather5Entity {
        /**
         * now : {"hum":"82","vis":"10","pres":"1008","pcpn":"0.7","fl":"37","tmp":"29","cond":{"txt":"阵雨","code":"300"},"wind":{"sc":"微风","spd":"7","deg":"220","dir":"东南风"}}
         * basic : {"city":"深圳","update":{"loc":"2017-07-14 12:50","utc":"2017-07-14 04:50"},"lon":"114.08594513","id":"CN101280601","cnty":"中国","lat":"22.54700089"}
         * status : ok
         */
        private NowEntity now;
        private BasicEntity basic;
        private String status;

        public void setNow(NowEntity now) {
            this.now = now;
        }

        public void setBasic(BasicEntity basic) {
            this.basic = basic;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public NowEntity getNow() {
            return now;
        }

        public BasicEntity getBasic() {
            return basic;
        }

        public String getStatus() {
            return status;
        }

        public class NowEntity {
            /**
             * hum : 82
             * vis : 10
             * pres : 1008
             * pcpn : 0.7
             * fl : 37
             * tmp : 29
             * cond : {"txt":"阵雨","code":"300"}
             * wind : {"sc":"微风","spd":"7","deg":"220","dir":"东南风"}
             */
            private String hum;
            private String vis;
            private String pres;
            private String pcpn;
            private String fl;
            private String tmp;
            private CondEntity cond;
            private WindEntity wind;

            public void setHum(String hum) {
                this.hum = hum;
            }

            public void setVis(String vis) {
                this.vis = vis;
            }

            public void setPres(String pres) {
                this.pres = pres;
            }

            public void setPcpn(String pcpn) {
                this.pcpn = pcpn;
            }

            public void setFl(String fl) {
                this.fl = fl;
            }

            public void setTmp(String tmp) {
                this.tmp = tmp;
            }

            public void setCond(CondEntity cond) {
                this.cond = cond;
            }

            public void setWind(WindEntity wind) {
                this.wind = wind;
            }

            public String getHum() {
                return hum;
            }

            public String getVis() {
                return vis;
            }

            public String getPres() {
                return pres;
            }

            public String getPcpn() {
                return pcpn;
            }

            public String getFl() {
                return fl;
            }

            public String getTmp() {
                return tmp;
            }

            public CondEntity getCond() {
                return cond;
            }

            public WindEntity getWind() {
                return wind;
            }

            public class CondEntity {
                /**
                 * txt : 阵雨
                 * code : 300
                 */
                private String txt;
                private String code;

                public void setTxt(String txt) {
                    this.txt = txt;
                }

                public void setCode(String code) {
                    this.code = code;
                }

                public String getTxt() {
                    return txt;
                }

                public String getCode() {
                    return code;
                }
            }

            public class WindEntity {
                /**
                 * sc : 微风
                 * spd : 7
                 * deg : 220
                 * dir : 东南风
                 */
                private String sc;
                private String spd;
                private String deg;
                private String dir;

                public void setSc(String sc) {
                    this.sc = sc;
                }

                public void setSpd(String spd) {
                    this.spd = spd;
                }

                public void setDeg(String deg) {
                    this.deg = deg;
                }

                public void setDir(String dir) {
                    this.dir = dir;
                }

                public String getSc() {
                    return sc;
                }

                public String getSpd() {
                    return spd;
                }

                public String getDeg() {
                    return deg;
                }

                public String getDir() {
                    return dir;
                }
            }
        }

        public class BasicEntity {
            /**
             * city : 深圳
             * update : {"loc":"2017-07-14 12:50","utc":"2017-07-14 04:50"}
             * lon : 114.08594513
             * id : CN101280601
             * cnty : 中国
             * lat : 22.54700089
             */
            private String city;
            private UpdateEntity update;
            private String lon;
            private String id;
            private String cnty;
            private String lat;

            public void setCity(String city) {
                this.city = city;
            }

            public void setUpdate(UpdateEntity update) {
                this.update = update;
            }

            public void setLon(String lon) {
                this.lon = lon;
            }

            public void setId(String id) {
                this.id = id;
            }

            public void setCnty(String cnty) {
                this.cnty = cnty;
            }

            public void setLat(String lat) {
                this.lat = lat;
            }

            public String getCity() {
                return city;
            }

            public UpdateEntity getUpdate() {
                return update;
            }

            public String getLon() {
                return lon;
            }

            public String getId() {
                return id;
            }

            public String getCnty() {
                return cnty;
            }

            public String getLat() {
                return lat;
            }

            public class UpdateEntity {
                /**
                 * loc : 2017-07-14 12:50
                 * utc : 2017-07-14 04:50
                 */
                private String loc;
                private String utc;

                public void setLoc(String loc) {
                    this.loc = loc;
                }

                public void setUtc(String utc) {
                    this.utc = utc;
                }

                public String getLoc() {
                    return loc;
                }

                public String getUtc() {
                    return utc;
                }
            }
        }
    }
}
