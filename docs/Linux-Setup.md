# Linux 服务器设置

## Install Software

安装基础软件

```
yum install -y vim tree htop pdsh epel-release ntp
```

安装 openjdk 11

```
yum install -y java-11-openjdk
```

## Linux 基本设置

###启用 NTP 服务：

```
sudo systemctl enable ntpd
sudo systemctl start ntpd
```

### 关闭 firewalld：

```
sudo systemctl stop firewalld
```

### 禁用 selinux

编辑 `/etc/selinux/config` 文件，修改如下内容：

```
SELINUX=disabled
```

## 网络设置

`/etc/sysctl.conf`

```
net.core.rmem_max = 16777216
net.core.wmem_max = 16777216
net.core.rmem_default = 16777216
net.core.wmem_default = 16777216
net.core.optmem_max = 40960
net.ipv4.tcp_rmem = 4096 87380 16777216
net.ipv4.tcp_wmem = 4096 65536 16777216
```

不重启生效：`sudo sysctl –p /etc/sysctl.conf`

## 用户资源限制

`/etc/security/limits.conf`

```
<user> - memlock unlimited
<user> - nofile  100000
<user> - nproc   32768
<user> - as      unlimited
```

*CentOS 7 或 systemd 需要在 `/etc/systemd/system.conf` 里修改*

`/etc/sysctl.conf`

```
vm.max_map_count = 1048575
```

### PAM Security Settings

对有些 Linux（如：Ubuntu）系统，需要启用 `pam_limits.so` 才可使 `/etc/security/limits.conf` 的配置生效。编辑 `/etc/pam.d/su` 文件：

```
session  required  pam_limits.so
```

