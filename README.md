# kafka-jslt-tansform

基于jstl的kafka connect的transform。使用jstl处理kafka json数据
JSLT是对JSON进行查询和转换语言，这个项目将其应用与kafka connect的transform。

JSLT GitHub: https://github.com/schibsted/jslt

# 功能更新

支持对Map<String,?>, Struct, String, byte[] 的输入处理,
输出类型改String为Map<String,?>

对于Source可以配合`org.apache.kafka.connect.json.JsonConverter`输出Json至kafka,
之前只能使用`StringConverter`


