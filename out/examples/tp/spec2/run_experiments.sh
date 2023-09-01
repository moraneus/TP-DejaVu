mkdir results 
gtime ./dejavu --specfile=spec2.qtl --logfile=log_10K.csv --prefile=spec2.pqtl --stat=false > res_10K
mv res_10K results
gtime ./dejavu --specfile=spec2.qtl --logfile=log_100K.csv --prefile=spec2.pqtl  --stat=false > res_100K
mv res_100K results
gtime ./dejavu --specfile=spec2.qtl --logfile=log_500K.csv --prefile=spec2.pqtl  --stat=false > res_500K
mv res_500K results
gtime ./dejavu --specfile=spec2.qtl --logfile=log_1M.csv --prefile=spec2.pqtl  --stat=false > res_1M
mv res_1M results
gtime ./dejavu --specfile=spec2.qtl --logfile=log_5M.csv --prefile=spec2.pqtl  --stat=false > res_5M
mv res_5M results