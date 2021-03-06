/**
 * Copyright © 2017 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jcustenborder.kafka.connect.kinesis;

import com.amazonaws.services.kinesis.model.Record;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.data.Timestamp;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.Map;

public class RecordConverter {

  public static final Schema SCHEMA_KINESIS_KEY;
  public static final Schema SCHEMA_KINESIS_VALUE;
  public static final String FIELD_SEQUENCE_NUMBER = "sequenceNumber";
  public static final String FIELD_APPROXIMATE_ARRIVAL_TIMESTAMP = "approximateArrivalTimestamp";
  public static final String FIELD_DATA = "data";
  public static final String FIELD_PARTITION_KEY = "partitionKey";
  public static final String FIELD_SHARD_ID = "shardId";
  public static final String FIELD_STREAM_NAME = "streamName";

  static {

    SCHEMA_KINESIS_KEY = SchemaBuilder.struct()
        .name("com.github.jcustenborder.kafka.connect.kinesis.KinesisKey")
        .doc("A partition key is used to group data by shard within a stream.\n")
        .field(RecordConverter.FIELD_PARTITION_KEY,
            SchemaBuilder.string()
                .doc("A partition key is used to group data by shard within a stream. The Streams service segregates " +
                    "the data records belonging to a stream into multiple shards, using the partition key associated " +
                    "with each data record to determine which shard a given data record belongs to. Partition keys are " +
                    "Unicode strings with a maximum length limit of 256 bytes. An MD5 hash function is used to map " +
                    "partition keys to 128-bit integer values and to map associated data records to shards. A " +
                    "partition key is specified by the applications putting the data into a stream. Identifies " +
                    "which shard in the stream the data record is assigned to. " +
                    "See [Record.getPartitionKey()](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/kinesis/model/Record.html#getPartitionKey--)")
                .optional()
                .build()
        )
        .build();

    SCHEMA_KINESIS_VALUE = SchemaBuilder.struct()
        .name("com.github.jcustenborder.kafka.connect.kinesis.KinesisValue")
        .doc("The unit of data of the Amazon Kinesis stream, which is composed of a sequence number, a partition key, and a data blob. See [Record](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/kinesis/model/Record.html)")
        .field(RecordConverter.FIELD_SEQUENCE_NUMBER,
            SchemaBuilder.string()
                .doc("The unique identifier of the record in the stream. See [Record.getSequenceNumber()](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/kinesis/model/Record.html#getSequenceNumber--)")
                .optional()
                .build()
        )
        .field(RecordConverter.FIELD_APPROXIMATE_ARRIVAL_TIMESTAMP,
            Timestamp.builder()
                .doc("The approximate time that the record was inserted into the stream. See [Record.getApproximateArrivalTimestamp()](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/kinesis/model/Record.html#getApproximateArrivalTimestamp--)")
                .optional()
                .build()
        )
        .field(RecordConverter.FIELD_DATA,
            SchemaBuilder.bytes()
                .doc("The data blob. See [Record.getData()](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/kinesis/model/Record.html#getData--)")
                .optional()
                .build()
        )
        .field(RecordConverter.FIELD_PARTITION_KEY,
            SchemaBuilder.string()
                .doc("A partition key is used to group data by shard within a stream. The Streams service segregates " +
                    "the data records belonging to a stream into multiple shards, using the partition key associated " +
                    "with each data record to determine which shard a given data record belongs to. Partition keys are " +
                    "Unicode strings with a maximum length limit of 256 bytes. An MD5 hash function is used to map " +
                    "partition keys to 128-bit integer values and to map associated data records to shards. A " +
                    "partition key is specified by the applications putting the data into a stream. Identifies " +
                    "which shard in the stream the data record is assigned to. " +
                    "See [Record.getPartitionKey()](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/kinesis/model/Record.html#getPartitionKey--)")
                .optional()
                .build()
        )
        .field(FIELD_SHARD_ID,
            SchemaBuilder.string()
                .doc("A shard is a uniquely identified group of data records in a stream. A stream is composed of one " +
                    "or more shards, each of which provides a fixed unit of capacity. Each shard can support up to 5 " +
                    "transactions per second for reads, up to a maximum total data read rate of 2 MB per second and up " +
                    "to 1,000 records per second for writes, up to a maximum total data write rate of 1 MB per second " +
                    "(including partition keys). The data capacity of your stream is a function of the number of shards " +
                    "that you specify for the stream. The total capacity of the stream is the sum of the capacities of " +
                    "its shards.")
                .optional()
                .build()
        )
        .field(FIELD_STREAM_NAME,
            SchemaBuilder.string()
                .doc("The name of the Kinesis stream.")
                .optional()
                .build()
        )
        .build();
  }

  private final KinesisSourceConnectorConfig config;

  public RecordConverter(KinesisSourceConnectorConfig config) {
    this.config = config;
  }

  public SourceRecord sourceRecord(final String streamName, final String shardId, Record record) {
    byte[] data = new byte[record.getData().remaining()];
    record.getData().get(data);
    Struct key = new Struct(RecordConverter.SCHEMA_KINESIS_KEY)
        .put(RecordConverter.FIELD_PARTITION_KEY, record.getPartitionKey());
    Struct value = new Struct(RecordConverter.SCHEMA_KINESIS_VALUE)
        .put(RecordConverter.FIELD_SEQUENCE_NUMBER, record.getSequenceNumber())
        .put(RecordConverter.FIELD_APPROXIMATE_ARRIVAL_TIMESTAMP, record.getApproximateArrivalTimestamp())
        .put(RecordConverter.FIELD_PARTITION_KEY, record.getPartitionKey())
        .put(RecordConverter.FIELD_DATA, data)
        .put(RecordConverter.FIELD_STREAM_NAME, streamName)
        .put(RecordConverter.FIELD_SHARD_ID, shardId);

    final Map<String, Object> sourcePartition = ImmutableMap.of(RecordConverter.FIELD_SHARD_ID, this.config.kinesisShardId);
    final Map<String, Object> sourceOffset = ImmutableMap.of(RecordConverter.FIELD_SEQUENCE_NUMBER, record.getSequenceNumber());

    final SourceRecord sourceRecord = new SourceRecord(
        sourcePartition,
        sourceOffset,
        this.config.kafkaTopic,
        null,
        RecordConverter.SCHEMA_KINESIS_KEY,
        key,
        RecordConverter.SCHEMA_KINESIS_VALUE,
        value,
        record.getApproximateArrivalTimestamp().getTime()
    );

    return sourceRecord;
  }

}
