import 'package:json_annotation/json_annotation.dart';


part 'test_model.g.dart';



@JsonSerializable(explicitToJson: true)
class TestModel {
  TestModel(this.missionPatchUrl,this.articleUrl,this.articleUrl2,this.articleUrl3);

  @JsonKey(name: "mission_patch")
  String? missionPatchUrl;

  @JsonKey(name: "article_link")
  String? articleUrl;

  @JsonKey(name: "articleUrl2")
  String? articleUrl2;

  @JsonKey(name: "articleUrl3")
  String? articleUrl3;

  factory TestModel.fromJson(Map<String, dynamic> json) => _$TestModelFromJson(json);

  Map<String, dynamic> toJson() => _$TestModelToJson(this);
}