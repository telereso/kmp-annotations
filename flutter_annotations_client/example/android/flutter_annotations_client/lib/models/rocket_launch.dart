import 'package:json_annotation/json_annotation.dart';
import 'package:flutter_annotations_client/models/rocket.dart';
import 'package:flutter_annotations_client/models/links.dart';

part 'rocket_launch.g.dart';

enum Type { FIRST,SECOND }

@JsonSerializable(explicitToJson: true)
class RocketLaunch {
  RocketLaunch(this.flightNumber,this.type,this.mission_name,this.launchYear,this.launchDateUTC,this.rocket,this.details,this.launchSuccess,this.links);

  @JsonKey(name: "flight_number")
  int? flightNumber;

  @JsonKey(name: "type")
  Type? type;

  @JsonKey(name: "mission_name")
  String? mission_name;

  @JsonKey(name: "launch_year")
  int? launchYear;

  @JsonKey(name: "launch_date_utc")
  String? launchDateUTC;

  @JsonKey(name: "rocket")
  Rocket? rocket;

  @JsonKey(name: "details")
  String? details;

  @JsonKey(name: "launch_success")
  bool? launchSuccess;

  @JsonKey(name: "links")
  Links? links;

  factory RocketLaunch.fromJson(Map<String, dynamic> json) => _$RocketLaunchFromJson(json);

  Map<String, dynamic> toJson() => _$RocketLaunchToJson(this);
}