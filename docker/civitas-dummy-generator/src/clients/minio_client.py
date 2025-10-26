"""MinIO client for object storage operations."""

import io
from datetime import datetime
from typing import Optional
import boto3
from botocore.exceptions import ClientError
from src.config.settings import settings
from src.utils.logging import get_logger

logger = get_logger(__name__)


class MinIOClient:
    """MinIO/S3 client for file operations."""
    
    def __init__(self):
        self.endpoint = f"http://{settings.minio_endpoint}"
        self.access_key = settings.minio_access_key
        self.secret_key = settings.minio_secret_key
        self.bucket_name = settings.minio_bucket
        self.secure = settings.minio_secure
        
        # Initialize boto3 client
        self.client = boto3.client(
            's3',
            endpoint_url=self.endpoint,
            aws_access_key_id=self.access_key,
            aws_secret_access_key=self.secret_key,
            region_name='us-east-1'  # MinIO doesn't care about region
        )
        
        # Ensure bucket exists
        self._ensure_bucket_exists()
    
    def _ensure_bucket_exists(self):
        """Ensure the bucket exists, create if it doesn't."""
        try:
            self.client.head_bucket(Bucket=self.bucket_name)
            logger.info(f"Bucket '{self.bucket_name}' exists")
        except ClientError as e:
            error_code = int(e.response['Error']['Code'])
            if error_code == 404:
                try:
                    self.client.create_bucket(Bucket=self.bucket_name)
                    logger.info(f"Created bucket '{self.bucket_name}'")
                except ClientError as create_error:
                    logger.error(f"Failed to create bucket: {create_error}")
                    raise
            else:
                logger.error(f"Error checking bucket: {e}")
                raise
    
    def upload_csv_content(self, content: str, filename: str) -> bool:
        """
        Upload CSV content to MinIO.
        
        Args:
            content: CSV content as string
            filename: Name of the file to create
        
        Returns:
            True if successful, False otherwise
        """
        try:
            # Convert string to bytes
            csv_bytes = content.encode('utf-8')
            
            # Upload to MinIO
            self.client.put_object(
                Bucket=self.bucket_name,
                Key=filename,
                Body=csv_bytes,
                ContentType='text/csv'
            )
            
            logger.info(f"Successfully uploaded {filename} to MinIO")
            return True
            
        except ClientError as e:
            logger.error(f"Failed to upload {filename} to MinIO: {e}")
            return False
    
    def upload_file(self, file_path: str, object_name: Optional[str] = None) -> bool:
        """
        Upload a file to MinIO.
        
        Args:
            file_path: Path to file to upload
            object_name: S3 object name. If not specified, file_path is used
        
        Returns:
            True if successful, False otherwise
        """
        if object_name is None:
            object_name = file_path
        
        try:
            self.client.upload_file(file_path, self.bucket_name, object_name)
            logger.info(f"Successfully uploaded {file_path} as {object_name}")
            return True
        except ClientError as e:
            logger.error(f"Failed to upload {file_path}: {e}")
            return False
    
    def download_file(self, object_name: str, file_path: str) -> bool:
        """
        Download a file from MinIO.
        
        Args:
            object_name: S3 object name to download
            file_path: Local path to save the file
        
        Returns:
            True if successful, False otherwise
        """
        try:
            self.client.download_file(self.bucket_name, object_name, file_path)
            logger.info(f"Successfully downloaded {object_name} to {file_path}")
            return True
        except ClientError as e:
            logger.error(f"Failed to download {object_name}: {e}")
            return False
    
    def list_objects(self, prefix: str = "") -> list:
        """
        List objects in the bucket.
        
        Args:
            prefix: Only list objects with this prefix
        
        Returns:
            List of object names
        """
        try:
            response = self.client.list_objects_v2(
                Bucket=self.bucket_name,
                Prefix=prefix
            )
            
            if 'Contents' in response:
                return [obj['Key'] for obj in response['Contents']]
            else:
                return []
                
        except ClientError as e:
            logger.error(f"Failed to list objects: {e}")
            return []
    
    def delete_object(self, object_name: str) -> bool:
        """
        Delete an object from MinIO.
        
        Args:
            object_name: Name of object to delete
        
        Returns:
            True if successful, False otherwise
        """
        try:
            self.client.delete_object(Bucket=self.bucket_name, Key=object_name)
            logger.info(f"Successfully deleted {object_name}")
            return True
        except ClientError as e:
            logger.error(f"Failed to delete {object_name}: {e}")
            return False
    
    def get_object_content(self, object_name: str) -> Optional[str]:
        """
        Get object content as string.
        
        Args:
            object_name: Name of object to retrieve
        
        Returns:
            Object content as string, or None if failed
        """
        try:
            response = self.client.get_object(Bucket=self.bucket_name, Key=object_name)
            content = response['Body'].read().decode('utf-8')
            return content
        except ClientError as e:
            logger.error(f"Failed to get content of {object_name}: {e}")
            return None


# Global client instance
minio_client = MinIOClient()
