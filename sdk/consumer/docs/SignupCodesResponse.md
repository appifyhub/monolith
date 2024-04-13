# SignupCodesResponse

The response containing all signup codes

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**signup_codes** | [**List[SignupCodeResponse]**](SignupCodeResponse.md) |  | 
**max_signup_codes** | **int** | The maximum number of signup codes allowed in this project | 

## Example

```python
from appifyhub.models.signup_codes_response import SignupCodesResponse

# TODO update the JSON string below
json = "{}"
# create an instance of SignupCodesResponse from a JSON string
signup_codes_response_instance = SignupCodesResponse.from_json(json)
# print the JSON string representation of the object
print(SignupCodesResponse.to_json())

# convert the object into a dict
signup_codes_response_dict = signup_codes_response_instance.to_dict()
# create an instance of SignupCodesResponse from a dict
signup_codes_response_from_dict = SignupCodesResponse.from_dict(signup_codes_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


