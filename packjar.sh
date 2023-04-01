bash build.sh
bash makedoc.sh
jar cvf app.jar -C bin miningsim
jar uvf app.jar doc src
mkdir tmp-packjar
mkdir -p tmp-packjar/META-INF
echo "Manifest-Version: 1.0" > ./tmp-packjar/META-INF/MANIFEST.MF
echo "Main-Class: miningsim.Main" >> ./tmp-packjar/META-INF/MANIFEST.MF
jar uvmf tmp-packjar/META-INF/MANIFEST.MF app.jar
rm -rf tmp-packjar
