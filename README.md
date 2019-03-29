# kafka-jslt-tansform

基于jstl的kafka connect的transform。使用jstl处理kafka json数据
JSLT是对JSON进行查询和转换语言，这个项目将其应用与kafka connect的transform。

JSLT GitHub: https://github.com/schibsted/jslt

# elgca.kafka.connect.JSLTTransformation

同`ExtractField`一样,`JSLTTransformation`可以分别对`key`和`value`使用

| class | type |
| --- | --- |
|`elgca.kafka.connect.JSLTTransformation$Key`| key|
|`elgca.kafka.connect.JSLTTransformation$Value`| value|

配置参数

| name | comment |
| --- | --- |
| `script.url` | 通过url获取`jslt`脚本,不能与`script.text`同时使用 |
| `script.text` | `jslt`脚本,不能与`script.text`同时使用 |
| `charset` | 获取的`jslt`脚本编码，如果收到的是byte[]转为json字符串时候的编码,默认UTF-8 |
