# MailgunConfigDto

Mailgun account configuration, primarily used for emails

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**api_key** | **str** | An active API key | 
**domain** | **str** | A verified domain | 
**sender_name** | **str** | The name of the default sender | 
**sender_email** | **str** | The email of the default sender | 

## Example

```python
from appifyhub.models.mailgun_config_dto import MailgunConfigDto

# TODO update the JSON string below
json = "{}"
# create an instance of MailgunConfigDto from a JSON string
mailgun_config_dto_instance = MailgunConfigDto.from_json(json)
# print the JSON string representation of the object
print(MailgunConfigDto.to_json())

# convert the object into a dict
mailgun_config_dto_dict = mailgun_config_dto_instance.to_dict()
# create an instance of MailgunConfigDto from a dict
mailgun_config_dto_form_dict = mailgun_config_dto.from_dict(mailgun_config_dto_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


