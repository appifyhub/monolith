# coding: utf-8

"""
    Appify Hub's Consumer API

    The full specification of the service's API used by the end-users.

    The version of the OpenAPI document: Latest
    Contact: contact@appifyhub.com
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


from __future__ import annotations
import json
from enum import Enum
from typing_extensions import Self


class UserType(str, Enum):
    """
    The type of the creator
    """

    """
    allowed enum values
    """
    PERSONAL = 'PERSONAL'
    ORGANIZATION = 'ORGANIZATION'

    @classmethod
    def from_json(cls, json_str: str) -> Self:
        """Create an instance of UserType from a JSON string"""
        return cls(json.loads(json_str))

