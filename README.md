```
======================================
======================================
======================================

login the HOST
export HOST=smp5.popiang.com
ssh -i ~/.ssh/id_rsa_do01 root@$HOST

sudo add-apt-repository ppa:git-core/ppa
<enter>
sudo apt-get update
sudo apt-get upgrade -y
<enter>
sudo apt install git
git --version
>>> 2.31.1
git config --global init.defaultBranch main

adduser num
<<< password
usermod -aG sudo num
rsync --archive --chown=num:num ~/.ssh /home/num

exit

===================
======= create git user
ssh -i ~/.ssh/id_rsa_do01 num@$HOST
export HOST=dip3.popiang.com

git config --global init.defaultBranch main

sudo adduser --disabled-password git

sudo mkdir /home/git/.ssh
sudo chown git.git /home/git/.ssh
sudo chmod 700 /home/git/.ssh
sudo vi /home/git/.ssh/authorized_keys

======= create a bare repo
sudo mkdir /repo
sudo chown git.git /repo

cd /repo
sudo git init --bare test1.git
sudo chown -R git.git /repo/test1.git

exit

======= test create repo
mkdir test1
cd test1
git init
vi README.md
git add .
git commit -m 'Initial commit'
git branch -M main
git remote add origin git@$HOST:/repo/test1.git
export GIT_SSH_COMMAND='ssh -o IdentitiesOnly=yes -i ~/.ssh/id_rsa_do01'
git push -u origin main

======= test clone repo
git clone git@$HOST:/repo/test1.git test1a
cd test1a
vim README.md
git commit -am 'Fix for README file'
git push origin main

================================ install golang
ssh -i ~/.ssh/id_rsa_do01 num@$HOST
export HOST=dip3.popiang.com

wget https://golang.org/dl/go1.16.4.linux-amd64.tar.gz

rm -rf /usr/local/go
sudo tar -C /usr/local -xzf go1.16.4.linux-amd64.tar.gz

vi $HOME/.profile
>>>>> add
PATH=$PATH:/usr/local/go/bin

source .profile
go version
>>>> go1.16.4

================================ install goweb
git clone https://github.com/choompol-boonmee/dip-train1.git
cp -Rf dip-train1/* .

cd goweb
sed "s/___HOST___/$HOST/" main.go > /tmp/main.go
cp /tmp/main.go main.go

go build -o build/attend
./build/attend

check web
http://dip3.popiang.com:8080/attend/A0001/com01:0/FN/LB

^C // stop web

================================ goweb autostart
sudo cp attend.service /etc/systemd/system/.

sudo systemctl daemon-reload
sudo systemctl enable attend
sudo systemctl restart attend
sudo systemctl status attend

check web
http://dip3.popiang.com:8080/attend/A0001/com01:0/FN/LB

======================== HTTPS WEB SERVER

sudo apt-get install -y nginx
sudo systemctl status nginx

check web
http://dip3.popiang.com/

sudo mkdir -p /var/www/$HOST/html
sudo chown -R $USER:$USER /var/www/$HOST/html
sudo chmod -R 755 /var/www/$HOST

cp index.html /var/www/$HOST/html/index.html

sudo sed "s/hostname/$HOST/" config1 > /tmp/cfg
sudo cp /tmp/cfg /etc/nginx/sites-available/$HOST

sudo ln -s /etc/nginx/sites-available/$HOST /etc/nginx/sites-enabled/

sed "s/# server_names_hash/server_names_hash/" /etc/nginx/nginx.conf > /tmp/nginx.conf
sudo cp -f /tmp/nginx.conf /etc/nginx/nginx.conf

sudo nginx -t
sudo systemctl restart nginx
sudo systemctl reload nginx

check web
http://dip3.popiang.com/

sudo apt install -y python3-certbot-nginx
sudo certbot --nginx -d $HOST

email
A
N
1

sudo nginx -t
sudo systemctl restart nginx

check web
https://dip3.popiang.com/attend/A0001/com01:0/FN/LB

cd ..
======================================= WEB STYLE
cd html

cp webcam.min.js /var/www/$HOST/html
cp -R js /var/www/$HOST/html
cp -R css /var/www/$HOST/html
cp -R images /var/www/$HOST/html
cp -R Fonts /var/www/$HOST/html

check web
https://dip3.popiang.com/attend/A0001/com01:0/FN/LB

cd ..

======================================= go engine
cd goeng
sed "s/___HOST___/$HOST/" main.go > /tmp/main.go
cp /tmp/main.go .
go build -o build/linux/gorecv

export ATTENDID=A0001
mkdir .cfg
touch .cfg/ATTENDID
./build/linux/gorecv

check web
https://smp6.popiang.com/attend/A0001/com01:0/FN/LB
============ test checkin in ./rdf/ev/evyyyymmdd.ttl

^C

======================================= build windows version
export GOOS=windows
go build -o build/windows/gorecv.exe

```
