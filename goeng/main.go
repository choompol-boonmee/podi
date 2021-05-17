package main

import (
        "fmt"
        "io"
		"io/ioutil"
        "os"
//		"path/filepath"
        "bufio"
        "strings"
        "net/http"
        "context"
		"encoding/base64"
        mrand "math/rand"
        "time"
        "github.com/libp2p/go-libp2p"
        "github.com/libp2p/go-libp2p-core/network"
        ma "github.com/multiformats/go-multiaddr"
        "github.com/libp2p/go-libp2p-core/host"
        ps "github.com/libp2p/go-libp2p-core/peer"
        "github.com/libp2p/go-libp2p-core/crypto"

)

var hostname = "___HOST___"

var path_getid string = "/attend/getid"
var url_pref string = "http://"+hostname+":8080"

var h3,h2 host.Host;
var id2 string;
var sndID,rcvID string;
var attID string;

//func setRecv(c1 chan string) {
func setRecv(c1 chan map[string]string) {

        fmt.Printf("Start Receiver...%s %s\n", url_pref, path_getid)

        r := mrand.New(mrand.NewSource(time.Now().UnixNano()))
        prv1, _, _ := crypto.GenerateKeyPairWithReader(crypto.RSA, 2048, r)

        h3, _ = libp2p.New(context.Background(), libp2p.Identity(prv1),
                libp2p.ListenAddrStrings("/ip4/0.0.0.0/tcp/9003"),
                libp2p.EnableRelay())

        rcvID0 := h3.ID().Pretty()
        fmt.Printf("rcvID[%s]\n", rcvID0)

        resp, _ := http.Get(url_pref+path_getid+"?rcvID="+rcvID0+
				"&ATTENDID="+attID)
        defer resp.Body.Close()
        body, _ := io.ReadAll(resp.Body)
        sndID = string(body)

        addr, _ := ma.NewMultiaddr("/dns4/"+hostname+"/tcp/9002/p2p/"+sndID)
        pe, _ := ps.AddrInfoFromP2pAddr(addr)
        if err := h3.Connect(context.Background(), *pe); err != nil { panic(err) }

        h3.SetStreamHandler("/cats", func(s network.Stream) {

			rw := bufio.NewReadWriter(bufio.NewReader(s), bufio.NewWriter(s))

			att, _ := rw.ReadString('\n')
			rdf, _ := rw.ReadString('\n')
			mod, _ := rw.ReadString('\n')
			lat, _ := rw.ReadString('\n')
			lng, _ := rw.ReadString('\n')
			pic, _ := rw.ReadString('\n')

			if strings.HasPrefix(att,"att=") && strings.HasPrefix(rdf,"rdf=") && strings.HasPrefix(mod,"mod=") && strings.HasPrefix(lat,"lat=") && strings.HasPrefix(lng,"lng=") && strings.HasPrefix(pic,"pic=") {
				dt := make(map[string]string)
				dt["att"] = string([]rune(att)[4:len(att)-1])
				dt["rdf"] = string([]rune(rdf)[4:len(rdf)-1])
				dt["mod"] = string([]rune(mod)[4:len(mod)-1])
				dt["lat"] = string([]rune(lat)[4:len(lat)-1])
				dt["lng"] = string([]rune(lng)[4:len(lng)-1])
				dt["pic"] = string([]rune(pic)[4:len(pic)-1])
				writeRdf(dt)
				rw.WriteString("OK:"+tmst+"\n")

				c1 <- dt

//				rw.WriteString("OK:"+dt["rdf"]+"\n")

				fmt.Println("WRITE OK >>>>>>>>>>>>>>>")
				rw.Flush()

				
			}
			s.Close()
        })

}

var tmst string;

func writeRdf(dt map[string]string) {

	fmt.Println()
	fmt.Printf("ATT: %s\n", dt["att"])
	fmt.Printf("RDF: %s\n", dt["rdf"])
	fmt.Printf("MOD: %s\n", dt["mod"])
	fmt.Printf("LAT: %s\n", dt["lat"])
	fmt.Printf("LNG: %s\n", dt["lng"])
//	fmt.Printf("PIC: %s\n", dt["pic"])

	currentTime := time.Now() 
	timeStampString := currentTime.Format("2006-01-02 15:04:05")
	
	who := dt["rdf"]
	day := timeStampString[0:10]
	tim := timeStampString[11:19]
fmt.Println("who:"+who+ " day:"+day+" tim:"+tim);

	layOut := "2006-01-02 15:04:05"
	timeStamp, _ := time.Parse(layOut, timeStampString)
	hr, mi, se := timeStamp.Clock()
	yr, mo, dy := currentTime.Year(), currentTime.Month(), currentTime.Day()
	fmt.Printf("A%04d%02d%02d %02d%02d%02d\n",yr,int(mo),dy,hr,mi,se)

	pg := fmt.Sprintf("ev%04d%02d%02d",yr,int(mo),dy)
	id := fmt.Sprintf("ev%04d%02d%02d:%02d%02d%02d",yr,int(mo),dy,hr,mi,se)
	pn := fmt.Sprintf("ev%04d%02d%02d_%02d%02d%02d",yr,int(mo),dy,hr,mi,se)

	tmst = fmt.Sprintf("%04d-%02d-%02d %02d:%02d:%02d",yr,int(mo),dy,hr,mi,se)

	pt := "./rdf/ev/"
	fmt.Println("make event dir: "+ pt)
	if _, er1 := os.Stat(pt); er1!=nil {
		_ = os.MkdirAll(pt, 0777)
	}

	pp := "./res/ev/"+pg+"/"
	fmt.Println("make pict dir: "+ pp)
	if _, er1 := os.Stat(pp); er1!=nil {
		_ = os.MkdirAll(pp, 0777)
	}

	fn := pt+pg+".ttl"
	fmt.Println("event file name: "+ fn)
	jp := pp+pn+".jpg"
	fmt.Println("event pict name: "+ jp)

	if _, er1 := os.Stat(fn); os.IsExist(er1) {
	}
	_, er1 := os.Stat(fn);
	if er1 != nil {
fmt.Println("========== checking2 file not exist: "+ fn)
		hd := "" +
		"@prefix vp: <http://ipthailand.go.th/rdf/itdep/voc-pred#> .\n" +
		"@prefix vo: <http://ipthailand.go.th/rdf/itdep/voc-obj#> .\n" +
		"@prefix com01: <http://ipthailand.go.th/rdf/itdep/com01#> .\n" +
		"@prefix "+pg+": <http://ipthailand.go.th/rdf/itdep/"+pg+"#> .\n" +
		"\n"
		ioutil.WriteFile(fn, []byte(hd), 0644)
		
		jpg1 := strings.Split(dt["pic"],",")[1]
		jpg2, _ := base64.StdEncoding.DecodeString(jpg1)
		ioutil.WriteFile(jp, jpg2, 0644)
	}

	if _, err := os.Stat(fn); os.IsNotExist(err) {
		hd := "" +
		"@prefix vp: <http://ipthailand.go.th/rdf/itdep/voc-pred#> .\n" +
		"@prefix vo: <http://ipthailand.go.th/rdf/itdep/voc-obj#> .\n" +
		"@prefix com01: <http://ipthailand.go.th/rdf/itdep/com01#> .\n" +
		"@prefix "+pg+": <http://ipthailand.go.th/rdf/itdep/"+pg+"#> .\n" +
		"\n"
		ioutil.WriteFile(fn, []byte(hd), 0644)
	}
	oc := "เข้างาน"
	if dt["mod"]=="leave" { oc = "ออกงาน" }
	tx := id+ "   vp:คือ   vo:"+oc+" ;\n" +
		"             vp:ใคร  "+ who + " ;\n" +
		"             vp:mode '"+ dt["mod"] +"' ;\n" +
		"             vp:วันที่  '"+ day + "' ;\n" +
		"             vp:เวลา '"+ tim + "' ;\n" +
		"             vp:พิกัด  '"+ dt["lat"] + "," + dt["lng"] + "' ;\n" +
		"             vp:ภาพ  '"+ jp + "' \n" +
		".\n"
	f, _ := os.OpenFile(fn, os.O_APPEND|os.O_WRONLY, 0644);
	_, _ = f.WriteString(tx);
	f.Close()
		
	jpg1 := strings.Split(dt["pic"],",")[1]
	jpg2, _ := base64.StdEncoding.DecodeString(jpg1)
	ioutil.WriteFile(jp, jpg2, 0644)
}

func main() {
		attID = os.Getenv("ATTENDID")
		fmt.Printf("ATTENDID: %s\n", attID)
		wd, _ := os.Getwd()
		fmt.Printf("WORK DIR: %s\n", wd)
		fAtt := wd+"/.cfg/ATTENDID"
		fmt.Printf("ATTEND FILE: %s\n", fAtt)
		if _, err := os.Stat(fAtt); os.IsNotExist(err) {
			fmt.Printf("!!! ATTENDID does not exists\n")
			return
		}

		c1 := make(chan map[string]string)
        setRecv(c1);
		cnt := 1
		max := 12 * 60
        for ;cnt>0; {
                select {
//                case dt := <-c1 :
                case <-c1 :
						fmt.Printf("CHECKIN...\n");
                case <-time.After(5*time.Second) :
						cnt++
						if(cnt>=max) {
							fmt.Printf("====== !!!!!!! RESET...\n");
							return
						}
						if cnt%5==0 {
                        	fmt.Print(".\n")
						} else {
                        	fmt.Print(".")
						}
						if _, err := os.Stat(fAtt); os.IsNotExist(err) {
							fmt.Printf("!!! ATTENDID does not exists\n")
							cnt = 0
						}
                }

        }
		fmt.Println("END ATTEND\n");
        time.Sleep(1 * time.Second)
}

