import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Sample {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Sample");
        job.setJarByClass(Sample.class);
        job.setMapperClass(TokenizerMapper.class);

        job.setReducerClass(sampleReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path("/user/mark/input"));
        FileOutputFormat.setOutputPath(job, new Path("/user/mark/output"));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public static class TokenizerMapper extends Mapper<LongWritable, Text, Text, Text> {

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            context.write(new Text(value.toString().split("\\|")[10]), value);
        }
    }

    public static class sampleReducer extends Reducer<Text, Text, Text, Text> {

        @Override
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            int count = 0;
            double ratingSum = 0, inComeSum = 0;
            int ratingCount = 0, inComeCount = 0;
            List<Review> reviews = new ArrayList<>();
            for (Text t : values) {
                if (count % 100 == 0) {
                    Review review = new Review(t.toString());
                    reviews.add(review);
                    if (review.getRating() != -1) {
                        ratingSum += review.getRating();
                        ratingCount += 1;
                    }
                    if (review.getUser_income() != -1) {
                        inComeSum += review.getUser_income();
                        inComeCount += 1;
                    }
                }
                count++;
            }
            reviews.sort((o1, o2) -> (int) (o1.getRating() - o2.getRating()));
            for (double i = 0.01 * reviews.size(); i < 0.99 * reviews.size(); i++) {
                if (reviews.get((int) i).getRating() == -1) {
                    reviews.get((int) i).setRating(ratingSum / ratingCount);
                }
                if (reviews.get((int) i).getUser_income() == -1) {
                    reviews.get((int) i).setUser_income(inComeSum / inComeCount);
                }
                context.write(null, new Text(reviews.get((int) i).toString()));
            }
        }
    }
}