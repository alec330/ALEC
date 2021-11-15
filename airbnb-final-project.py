#!/usr/bin/env python
# coding: utf-8

# In[12]:


# modules for research report
from datascience import *
import numpy as np
import random
import pandas as pd
import folium
get_ipython().run_line_magic('matplotlib', 'inline')
import matplotlib.pyplot as plots
plots.style.use('fivethirtyeight')

# module for YouTube video
from IPython.display import YouTubeVideo

# okpy config
from client.api.notebook import Notebook
ok = Notebook('airbnb-final-project.ok')
_ = ok.auth(inline=True)


# # Airbnb Listings and Evictions
# 
# The dataset you will be using is from [Inside Airbnb](http://insideairbnb.com/get-the-data.html), an independent investigatory project that
# collects and hosts substantial Airbnb data on more than 100 cities around the world. The data collected by Inside Airbnb are web-scraped from
# the Airbnb website on a monthly basis. Inside Airbnb was started to investigate the effects of Airbnb on affordable housing and gentrification.
# Its data are made public for free and open for use.  
# 
# We have prepared for you a random subset of Inside Airbnb data from San Francisco collected in June 2020. The data have been
# cleaned for your convenience: all missing values have been removed, and low-quality observations and variables have been filtered
# out. A brief descriptive summary of the dataset is provided below. 
# 
# We are aware that this dataset is potentially significantly larger (in both rows and columns) than other datasets for the project. As a result, 
# you will have many potential directions to conduct your analysis in. At the same time, it is very easy to become overwhelmed or lost with the data.
# We encourage you to reach out by posting your questions on the relevant Piazza thread, or by sending Angela (guanangela@berkeley.edu) or Alan
# (alanliang@berkeley.edu) an email if you need any help.
# 
# **NB: You may not copy any public analyses of this dataset. Doing so will result in a zero.**

# ## Summary
# >Airbnb offers a platform to connect hosts with guests for short-term or long-term lodging accommodations. Compared to similar firms offering vacation rental services
# such as VRBO or HomeAway, Airbnb is the largest and most prominent, with more than 7 million listings worldwide and 2 million people staying in one of its listings
# per night in 2018. Since its founding in 2008, hosts on the platform have served more than 750 million guests, and the firm has grown at an exponential rate globally
# pre-COVID.
# 
# 
# >The data presented are completely from web scraping the Airbnb website in June 2020 for random subset of listings in San Francisco. As a result, the data only contain
# information that a visitor to Airbnb’s site can see. This includes the `listings` table that records all Airbnb units and the `calendar` table that records the
# availabilities for the next 365 days and quoted price per night over the next year of each listing. What each table specifically describes will be gone over in the
# Data Description section below. Note that we do not observe Airbnb transactions or bookings, but only the dates that are available or unavailable through `calendar`.
# 
# 
# >The primary identifier for each listing is the `listing_id` or `id` column (the column name changes depending on the title). Each ID uniquely determines a listing,
# and every listing only has 1 ID. You can visit each listing's URL on Airbnb by going to https://www.airbnb.com/rooms/YOUR_ID_HERE with the id to look up the listing
# on the airbnb website.

# ## Data Description

# The dataset consists of many tables stored in the `data` folder. **You do not have use to all of the tables in your analysis.**
# 1. `listings` provides information on 2000 Airbnb listings in San Francisco. Each row is a unique listing.
# 2. `ratings` contains average ratings for the Airbnb listings across 6 categories and its overall rating. Guests who stay at an Airbnb are eligible to score a listing on each of the categories and on the overall score out of 5.
# 3. `calendar` contains each listing's availability and price over the next year. This data is the same as the calendar that pops up when users try to select the dates of a reservation for a particular listing. For example, the first row means that the listing with ID 40138 was not available on June 8th, 2020. The price per night of this listing is \\$67. 
# 4. `evictions` contains information on evictions in San Francisco, and may be useful if you are interested in determining relationships between Airbnbs and gentrification or evictions.
# 
# 
# There are a lot of columns for many of these datasets, and you probably will only use a few of them. We've selected some of the variables that may be of interest below:
# 
# `listings`:
# * `id`: listing ID.  You can visit each listing's URL on Airbnb by going to https://www.airbnb.com/rooms/YOUR_ID_HERE with the id to look up the listing on the airbnb website.
# * `Name`: listing or rental name
# * `neighborhood` and `neighbourhood_cleansed`: neighborhood of listing
# * `latitude`, `longtitude`: latitude and longitude of listing location. Note that for privacy reasons, this may be approximate.
# * `calculated_host_listings_count`: the number of different listings the host has on Airbnb.
# * `property_type`: type of property the listing is in (e.g. Apartment, Condo, House, etc)
# * `room_type`: type of place (e.g. entire home, private room, etc)
# * `accommodates`: max number of guests
# * `minimum_nights` and `maximum_nights`: minimum and maximum number of nights a reservation can be
# * `availability_X`: availability for the next X days (relative to the scraping date, June 8th, 2020)
# * `amenities`: a list of amenities provided by the listing. Note that each item is an iterable set
# 
# `ratings`: 
# * `review_scores_rating`: review score overall rating of listing. Note that on the Airbnb site the score is out of 5, but this value is out of 100. 
# * `review_scores_accuracy`: review score based on accuracy of listing. Note that on the Airbnb site the score is out of 5, but this value is out of 10. 
# * `review_scores_cleanliness`: review score based on clealiness of listing. Note that on the Airbnb site the score is out of 5, but this value is out of 10. 
# * `review_scores_checkin`: review score based on check-in of listing. Note that on the Airbnb site the score is out of 5, but this value is out of 10. 
# * `review_scores_communication`: review score based on communication with host. Note that on the Airbnb site the score is out of 5, but this value is out of 10. 
# * `review_scores_location`: review score based on location of listing. Note that on the Airbnb site the score is out of 5, but this value is out of 10. 
# 
# 
# `calendar`:
# * `listing_id`: ID of airbnb listing
# * `date`: date of the potential availability in question
# * `price`: price per night of listing in USD
# * `available`: true or false value representing whether the listing was available.
# 
# `evictions`:
# * `File Date`: date the eviction was reported 
# * `Neighborhood`: neighborhood in which the eviction occurred
# * `Longtitude` and `Latitude`: latitude and longitude of the listing
# * All other columns indicate the reason of the eviction. For example, if an eviction has `True` for the `Non Payment` column and `False` for all other columns, the eviction was due to non-payment. 

# ## Preview
# 
# The tables are loaded in the code cells below. Take some time to explore them!

# In[13]:


# Load the data for airbnb listings
listings = Table().read_table("data/listings.csv")
listings.show(20)


# In[14]:


# Load in ratings table
ratings = Table().read_table("data/ratings.csv")
ratings.show(70)


# In[15]:


# Load in the calendar table
calendar = Table().read_table("data/calendar.csv")
calendar


# In[16]:


# Load in the evictions table
evictions = Table().read_table("data/evictions.csv")


# <br>
# 
# # Research Report

# ## Introduction
# 
# For the final project, we have analyzed the Airbnb listing and evictions dataset. The dataset was collected from the website insideairbnb.com in June 2020, and it provides a random sample of 2000 Airbnbs in San Francisco. The data was separated into four different tables: `listings`, `calendar`, `ratings` and `evictions`. For the purpose of our project, we are going to focus only on the `listings`, `ratings` and `calendar` tables.
# 
# The `listings` table provides us the basic information on the Airbnb and its host. For example, it tells us the unique Listing IDs and names that Airbnb used to register the Airbnbs, as well as their locations. The table also provides us information of the host themselves; for instance, the 'host_is_superhost' column tells us whether the host of the Airbnb is a superhost. By comparing the price of the Airbnbs hosted by superhosts and non-superhosts, we can understand whether the superhost program raises the price of the superhost's Airbnb. By Airbnb's standard, a superhost needs to 1. Maintain a 4.8 overall rating. 2. completed 10+ stays. 3. less than 1% cancellation rate. 4. Have a 90% response rate.
# 
# The `calendar` table provides us information on the price of the Airbnb for the next whole year. We can know whether the Airbnb was available on a specific date. We are able to compute the average price of the Airbnb for the next year, which is essential when we are comparing the price of the Airbnbs hosted by a superhost and a non-superhost.
# 
# The `ratings` table only provide the overall ratings of the Airbnbs, but it was separated into different categories: the location rating of the Airbnbs, the accuracy of the ratings, and the cleanliness ratings of the Airbnb etc. The overall ratings in this dataset is out of 100, instead of out of 10 like other ratings. Therefore, as we proceed to the classifier, we will need to convert the ratings into standard unit.

# ## Hypothesis Testing and Prediction Questions
# 
# **For our hypothesis test, our null hypothesis is that the distribution of the average price of the Airbnbs that are hosted by a super-host and a non-super-host are the same. Any difference is due to chance. The alternative hypothesis is that in the population, the average price of the Airbnb hosted by a super-host, on average, is higher than the price of the Airbnb hosted by a non-super-host. We are trying to find out whether the superhosts tend to raise the price of their Airbnbs.** The test statistic we are planning to use is through A/B testing because we have to perform random permutation in order to see if it vary under the null hypothesis.  Any large value of the statistics will favor the alternative more. Also, we have decided to use 5% as our significance level.
# 
# **For our prediction questions, relatedly, we hope to predict whether an Airbnb is hosted by a superhost or non_superhost by using an Airbnb's cleanliness rating, overall review rating, and total number of reviews**. We believe that different catgories of reviews and the total number of reviews are the most important factors that determine whether or not a host is qualified for the superhost status. We are employing a K-nearest-neighbor classifier to complete this task, and we will be examining the accuracy of our classifier after.

# ## Exploratory Data Analysis

# **Table Requiring a Join Operation:**

# In[17]:


cal_org = calendar.group('listing_id',np.mean)
listing_cal = listings.join('id',cal_org,'listing_id')
host_price_id= listing_cal.select('host_is_superhost','price mean')
host_price_id.group('host_is_superhost', np.mean)


# First from the `calendar` data table, we group the listing_id together to see the average price of each airbnb of the listing ID and defined this new table as `cal_org`. From there we then join this new table that we just created to the listings data table by the IDs to create `listing_cal`. In the new `listing_cal` table, we need to filter out the columns that we needed, so we created a new table called `host_price_id` which displays host_is_superhost and price mean. We then use this new table of `host_price_id` to investigate whether our prediction of the average price of the Airbnbs that are hosted by a super-host is higher than the non super-host. We will determine whether this difference between average prices is significant in the Hypothesis Testing Section.

# **Aggregated Data Table:**

# In[18]:


# Use this cell to generate your aggregated data table
listings.pivot('neighbourhood', 'host_is_superhost')


# As this is a random sample, we would expect that there is an even distribution numbers of superhost and non_superhost. We decided to pivot neighbourhood and host_is_superhost in order to compare the number of superhost and non_superhost in each neghborhood. As we can see, the numbers are roughly the same in each neighborhood. 

# **Qualitative Plot:**

# In[19]:


# Use this cell to generate your qualitative plot
host_price_id.group('host_is_superhost').barh(0,1)


# Following up with our aggregated data, we plot the graph above to show the total number of superhosts and non-superhost. As we are using A/B testing to answer our hypothesis question, we need to ensure that there are a similar total number of superhost and non-superhost. Otherwise, the result of hypothesis test will be invalid. Therefore, we grouped the column `host_is_superhost` and observed the difference between superhosts and non-superhosts. As we have noticed, the ratio of superhost to non_superhost is almost 1:1.

# **Quantitative Plot:**

# In[20]:


# Use this cell to generate your quantitative plot
xnew_trial = listings.join('id', cal_org, 'listing_id').select('host_is_superhost', 'id', 'cleaning_fee', 'number_of_reviews').join('id', ratings, 'listing_id')

def su(arr):
    return (arr - np.average(arr)) / np.std(arr)
su_tbl = Table().with_columns('SU review',su(xnew_trial.column(3)),'SU rating',su(xnew_trial.column(4)),'SU clean',su(xnew_trial.column(6)))
su_tbl_with_id = su_tbl.with_column('id', xnew_trial.column('id')).with_column('host_is_superhost', xnew_trial.column('host_is_superhost'))
su_tbl_with_id

def changer(array):
    if array == 'f':
        return 0
    elif array == 't':
        return 1
new_t = su_tbl_with_id.with_column('True_false', xnew_trial.apply(changer, 'host_is_superhost'))
new_t

import matplotlib.pyplot as plots 
from mpl_toolkits.mplot3d import Axes3D
fig = plots.figure(figsize = (8,8))
ax = Axes3D(fig)
ax.scatter(new_t.column('SU review'), new_t.column('SU rating'), new_t.column('SU clean'), c = new_t.column('True_false'), cmap = 'viridis', s = 50)
ax.set_xlabel('Number of reviews')
ax.set_ylabel('Overall ratings')
ax.set_zlabel('Cleanliness ratings')


# The above graph is a 3D plot that visualizes the relationship in between the 3 attributes that we are using. All of the attributes are converted into standard units for a more accurate analysis. We can clearly see that the superhost (yellow dots) are mainly on the right side on the graph and the non_superhost (purple dots) are on the left side. Therefore, we believe that our kNN classifier will be able to produce accurate predictions.

# ## Hypothesis Testing

# Next, we will perform a hypothesis test using the test statistic of the A/B testing procedure. We will see if the prices of the superhost and non superhost are the same. The null hypothesis is that the prices of superhost and non superhost is the same and any difference is due to chance. Our alternative hypothesis is that the prices are not the same in which the superhost prices are higher than the non superhost.
# 
# We use the test statistic of A/B testing due to the fact that since the null hypothesis states that superhost and non superhost prices are the same, we will use random permutations to test under the null. Our test will be the difference of the prices of superhost and non superhost and any large values of the test statistics will favor the alternative more. We will use a cutoff value of 5% significance level.
# 

# In[23]:


# set the random seed so that results are reproducible
np.random.seed(1231)

# this function computes the difference of means for the sampling that we will be doing with 10,000 repetitions.
def difference_of_means(table, label, group_label):
    """Takes in: name of table, a column label with numerical variable,
    column label of group-label variable
    Returns: Difference of means of the two groups"""
    #filtering columns we need
    sort = table.select(label, group_label)
    # table that contains the average of the group_label
    means_table = sort.group(group_label, np.mean)
    # an array of of groupe_label means
    means = means_table.column(1)
    return means.item(1) - means.item(0)


# Using the above function, we compute the observed test statistic for our hypothesis test
observed_diff = difference_of_means(host_price_id, 'price mean', 'host_is_superhost')

# A function that simulate one shuffle of the group_label column in which it computes the difference of means after
def one_simulated_difference(table, label, group_label):
    
    #shuffling of the column we choose
    shuffled_labels = table.sample(with_replacement = False).column(group_label)
    
    # adding the shuffled column arrays to the computed table with a new column called "Shuffled Label"
    shuffled_table = table.select(label).with_column('Shuffled Label', shuffled_labels)
    
    return difference_of_means(shuffled_table, label, 'Shuffled Label')

# We are now going to perform the A/B testing procedure for 10000 repetition under the the null

means_diff = make_array()

repetitions = 10000
for i in np.arange(repetitions):
    simulate = one_simulated_difference(host_price_id, 'price mean', 'host_is_superhost')
    means_diff = np.append(means_diff, simulate)
    
# Here is the visualization for our results
Table().with_column('Difference Bteween Means', means_diff).hist()


# In[24]:


# p-value for our test-statistic and a visualization showing our observed_stats within the simulation
np.random.seed(1231)
empirical_P = np.count_nonzero(means_diff >= observed_diff) / 10000
empirical_P
Table().with_column('Difference Between Means', means_diff).hist()
plots.scatter(observed_diff, 0, color = 'red', s = 40)
plots.title('Prediction Under the Null Hypothesis')
print('Observed Difference:', observed_diff, 'P-value:', empirical_P)

#percentile according to the 5% significance level
right = percentile(97.5, means_diff)
left = percentile(2.5, means_diff)
print('95% confidence Interval: ', right, ',', left)


# As you can see, we have enough evidence to fail to reject the null hypothesis due to that fact that our observed difference of 7.4 falls within the interval of the 95% confidence interval of -13.43 to 13.21. Morevoer, we have a P-value of 0.1331. With our cut-off value of 5% significance level, we are confident to conclude that it is more consistent with the null hypothesis. This means that the prices of the Airbnbs between the non super-host vs super-host seems to be about the same price regardless of the many requirements that the super-host must have.

# ## Prediction

# Next, we will be predicting the superhost status of an Airbnb host. We have selected to use the KNN classifier because we are predicting a categorical outcome. We believe there are three attributes that can determine the superhost status the best, which are the 'total number of reviews', the 'cleanliness ratings', and the 'overall ratings'. Based on our quantitative plot, we expect our classifier to perform well since we could see there is somewhat a pattern showing that majority of the superhosts are on the right side of the graph and non-superhosts are on the left side.

# In[25]:


#Combining our definition/attributes of superhosts together in one table
xnew_trial = listings.join('id', cal_org, 'listing_id').select('host_is_superhost', 'id', 'cleaning_fee', 'number_of_reviews').join('id', ratings, 'listing_id')
xnew_trial


# In[26]:


#Converting the attributes into standard units
def su(arr):
    return (arr - np.average(arr)) / np.std(arr)
su_tbl = Table().with_columns('SU review',su(xnew_trial.column(3)),'SU rating',su(xnew_trial.column(4)),'SU clean',su(xnew_trial.column(6)))
su_tbl_with_id = su_tbl.with_column('id', xnew_trial.column('id')).with_column('host_is_superhost', xnew_trial.column('host_is_superhost'))
su_tbl_with_id


# In[27]:


# Converting true/false into integers in order to generate our 3D-plot
def changer(array):
    if array == 'f':
        return 0
    elif array == 't':
        return 1
new_t = su_tbl_with_id.with_column('True_false', xnew_trial.apply(changer, 'host_is_superhost'))
new_t


# In[28]:


import matplotlib.pyplot as plots 
from mpl_toolkits.mplot3d import Axes3D
fig = plots.figure(figsize = (8,8))
ax = Axes3D(fig)
ax.scatter(new_t.column('SU review'), new_t.column('SU rating'), new_t.column('SU clean'), c = new_t.column('True_false'), cmap = 'viridis', s = 50)
ax.set_xlabel('Number of reviews')
ax.set_ylabel('Overall ratings')
ax.set_zlabel('Cleanliness ratings')


# In[37]:


## shuffling all the airbnbs and separating them into train set and test sets with a specific proportion
random.seed(123)
shuffled_new_table = new_t.sample(with_replacement = False)
train_test_proportion = 0.8
num_airbnb = shuffled_new_table.num_rows
train_num = int(num_airbnb * train_test_proportion)
test_num = num_airbnb - train_num

#showing the train set
train_airbnb1 = shuffled_new_table.take(np.arange(train_num))
test_airbnb1 = shuffled_new_table.take(np.arange(test_num))
train_airbnb1


# In[38]:


# Function defining the Euclidean distance formula:
np.random.seed(123)
def distance(arr1, arr2):
    return np.sqrt(np.sum((arr1 - arr2) ** 2))

# making an array for the features that we are using
my_features = make_array('SU review', 'SU rating', 'SU clean')

# A function that converts all the rows to an array
def row_to_array(row, features):
    arr = make_array()
    for feature in features:
        arr = np.append(arr, row.item(feature))
    return arr

# The trial distance between first and second Airbnb to test to out whether the distance function works
distance((row_to_array(train_airbnb1.row(0), my_features)),(row_to_array(train_airbnb1.row(1), my_features)))


# In[39]:


# The classifier and the prediction of the first row of the test_airbnb table
np.random.seed(123)
def classify(row, k, train):
    test_row_features_array = row_to_array(row, my_features)
    distances = make_array()
    for train_row in train.rows:
        train_row_features_array = row_to_array(train_row, my_features)
        row_distance = distance(train_row_features_array, test_row_features_array)
        distances = np.append(distances, row_distance)
    train_with_distances = train.with_column('Distances', distances)
    nearest_neighbors = train_with_distances.sort('Distances').take(np.arange(k))
    most_common_label = nearest_neighbors.group('host_is_superhost').sort('count', descending = True).column('host_is_superhost').item(0)
    return most_common_label

classify(test_airbnb1.row(0), 3, train_airbnb1)


# In[40]:


# defining the three classify function to set our k as 3
np.random.seed(123)
def three_classify(row):
    return classify(row, 3, train_airbnb1)

# applying the function classify to the whole test_airbnb table

predicted_test_airbnb1 = test_airbnb1.with_column("prediction", test_airbnb1.apply(three_classify)).drop('True_false')
predicted_test_airbnb1.show(10)


# In[41]:


# The accuracy 
np.random.seed(123)
labels_correct = np.count_nonzero(predicted_test_airbnb1.column('prediction') == predicted_test_airbnb1.column('host_is_superhost'))
accuracy = labels_correct / test_airbnb1.num_rows
accuracy


# As we suspected, our k-NN classifier was accurate as it predicts around 80% of the superhost status correctly. We could not find any pattern of the misclassified hosts. We decided to pick a small value of k since the dots are very close to each other. If we picked a higher value of k, it might affect the accuracy of the classifier. One of the reasons why we were not able to get a higher accuracy is because since the Airbnb has its own definition on how to become a superhost and we have our own attributes that we believe that might determine it. These different attributes may play a part on making the accuracy lower. Therefore, there is some limits to the accuracy of our classifier.

# ## Conclusion
# 
# In conclusion, our research project attempted to find the difference between the price of an Airbnb hosted by a superhost and non-superhost. Using an A/B testing procedure, we showed that the prices of a superhost's Airbnb and non-superhost's Airbnb are the same. Therefore, it shows that the superhosts do not tend to use this priority to raise the price. We believe that this might be due to the fact that the Airbnb does not notify the hosts if they are officially classified as superhosts. Therefore, the hosts are not aware and did not raise the price.
# 
# For our prediction part, we were able to generate a high accuracy KNN classifier that predicts the superhost status by using the overall review ratings, the total number of reviews, and the cleanliness ratings. We might not be able to obtain a higher accuracy since our definition of superhost is different from Airbnb's. 
# 
# There are some biases that occurred in the dataset; for instance, non-selection bias. We used the ratings dataset in order to perform our prediction part; however, some of the people who booked the Airbnb might not give a review after. Therefore, their lack of representation in our data could severely bias our findings.

# ## Presentation
# 
# *In this section, you'll need to provide a link to your video presentation. If you've uploaded your presentation to YouTube,
# you can include the URL in the code below. We've provided an example to show you how to do this. Otherwise, provide the link
# in a markdown cell.*
# 
# **Link:** *Replace this text with a link to your video presentation*

# In[42]:


# Full Link: https://www.youtube.com/watch?v=BKgdDLrSC5s&feature=emb_logo
# Plug in string between "v=" and ""&feature":
YouTubeVideo('GYV63ADmJwQ')


# # Submission
# 
# *Just as with the other assignments in this course, please submit your research notebook to Okpy. We suggest that you
# submit often so that your progress is saved.*

# In[ ]:


# Run this line to submit your work
_ = ok.submit()


# In[ ]:




