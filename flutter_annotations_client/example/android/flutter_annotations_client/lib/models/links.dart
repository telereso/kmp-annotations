import 'package:json_annotation/json_annotation.dart';


part 'links.g.dart';



@JsonSerializable(explicitToJson: true)
class Links {
  Links(this.missionPatchUrl,this.articleUrl);

  @JsonKey(name: "mission_patch")
  String? missionPatchUrl;

  @JsonKey(name: "article_link")
  String? articleUrl;

  factory Links.fromJson(Map<String, dynamic> json) => _$LinksFromJson(json);

  Map<String, dynamic> toJson() => _$LinksToJson(this);
}