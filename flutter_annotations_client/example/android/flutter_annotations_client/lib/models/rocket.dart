import 'package:json_annotation/json_annotation.dart';


part 'rocket.g.dart';



@JsonSerializable(explicitToJson: true)
class Rocket {
  Rocket(this.id,this.name,this.type);

  @JsonKey(name: "rocket_id")
  String? id;

  @JsonKey(name: "rocket_name")
  String? name;

  @JsonKey(name: "rocket_type")
  String? type;

  factory Rocket.fromJson(Map<String, dynamic> json) => _$RocketFromJson(json);

  Map<String, dynamic> toJson() => _$RocketToJson(this);
}