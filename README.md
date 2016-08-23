# Tools for CCIO Image Caching and Manipulation Cluster #

**Tools for CCIO Image Cluster** is set of instruments for installing ImMan nodes on [DigitalOcean](https://m.do.co/c/9e592545f7b6) Droplets and DNS records on [CloudFlare](https://www.cloudflare.com/).

## Attributes ##

- -c - Cluster Name
- -s3a - AWS S3 Access Code
- -s3s - AWS S3 Secret Code
- -s3b - AWS S3 Bucket Name
- -do - Digital Ocean API Token
- -cft - CloudFlare API Token
- -cfe - CloudFlare API Email
- -cfz - CloudFlare Domain Zone, e.q. ccio.co
- -cfd - CloudFlare DNS sub domain, e.g. mycluster for mycluster.ccio.co
- -h - Print help

## Run Tools ##

```
java -jar imman-tools.jar -c TEST-CLUSTER -s3a S3AAA -s3s S3SSS -s3b BUCKET_NAME -do EEEEE -cft XXXXXX -cfe EEE@MMM.COM -cfd mycluster -cfz ccio.co
```

[Download imman-tools.jar here](https://github.com/CloudCluster/imman-tools/blob/master/src/main/bin/imman-tools.jar)

Once you'll create ImMan Cluster, you should find Cluster JSON file: **/opt/ccio/clusters/TEST-CLUSTER.json** which will contain all information about the cluster. This file will be used for future cluster adjustments.

More information on [Wiki](https://github.com/CloudCluster/imman-tools/wiki).