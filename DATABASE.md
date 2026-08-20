飞机售票系统

功能：
1.登录
2.查询(1.机票：出发地点、到达地点、出发日期+时间、到达日期+时间、航空公司、价格、时长、舱级2.订单：状态、时间、航司) => 只能查自己的order: where user_id = ${userId};
3.订票(1.商家：放票功能2.用户：订票功能)
4.支付(1.商家：收款2.用户：付款3.第三方：暂存)
5.核销(改变order状态)
6.退订(1.用户：收款2.第三方：退款)
7.改签(改变order指向的ticket，多退少补，限制改签时间，改签航司)

数据库：
## 1
user:id,username,password,real_name,age,email,phone,status,role,creat_at
eg:

## 2
flight:id,id_plane,id_airport_dep,id_airport_arr,code,datetime_dep,datetime_arr,region_dep,region_arr,distance,
seat_first_class,seat_business_class,seat_economy_class,price,cancellation_fee,gate,status
eg:
航班信息=>这里是狭义的航班：一趟在任何时间任何地点都唯一的一次飞行旅程
status = "未开始" 显示的都是首发datetime_dep
status = "已结束" || "取消" || "进行中" 显示的就是真实datetime_dep

## 3
channel:id,channel_name,api_gateway_url
eg:

## 4
order:id,flight_id,user_id,channel_id,code,total_price,total_tax,
pay_status,order_status,pay_time,issue_time,cancel_time,remark,create_at
eg:
飞行信息

## 5
ticket:id,order_id
eg:

## 6
route:flight_id,flight_datetime_dep,distance_remain,altitude,speed,
latitude(纬度),longitude(经度),time_remain,time_stamp
eg:

## 7
plane:id,id_airline,model_name,length,wingspan,height,max_takeoff_weight_kg,
max_landing_weight_kg,max_seat_first_class,max_seat_business_class,max_economy_class
eg:

## 8
airline:id,name
eg:

## 9
airport:id,name,region
eg:

## 10
passenger:user_id,passenger_id
eg:

数据量过大：
1.常查询列建索引=>二分查找
2.表分区=>内置分区功能
3.冷热表=>久远数据在别的库
4.分库分表=>MyCat ShardingSphere
5.异构索引=>其他搜索引擎
6.读写分离=>只读从库+写主库