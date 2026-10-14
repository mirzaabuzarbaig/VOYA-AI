"""
Script to create a legitimate, realistic travel cost benchmark dataset.
Based on real-world Indian domestic and international travel cost structures:
- Transport baseline (train, bus, flight, car)
- Nightly accommodation rates across tiers (Hostel, Hotel, Resort, Luxury Hotel)
- Per-diem food, local sightseeing, and activities
- Destination tier index (domestic vs international corridor)
- Seasonality (peak holiday months vs monsoon/off-peak)
- Realistic expenditure variance across travelers and duration
"""

import numpy as np
import pandas as pd
import os

def generate_travel_dataset(n_samples=1500, random_state=42):
    np.random.seed(random_state)
    
    national_destinations = [
        'Goa', 'Jaipur', 'Kerala', 'Manali', 'Mumbai', 
        'New Delhi', 'Bengaluru', 'Hyderabad', 'Udaipur', 'Varanasi'
    ]
    
    international_destinations = [
        'Dubai', 'Paris', 'Singapore', 'Bangkok', 
        'London', 'Tokyo', 'Bali', 'New York'
    ]
    
    accommodation_tiers = {
        'Hostel': {'national': 800, 'international': 2500},
        'Guesthouse': {'national': 1500, 'international': 4500},
        'Hotel': {'national': 3200, 'international': 8500},
        'Resort': {'national': 6000, 'international': 16000},
        'Luxury Hotel': {'national': 12000, 'international': 30000}
    }
    
    transport_bases = {
        'Train': {'national': 1200, 'international': 6000},
        'Bus': {'national': 800, 'international': 4000},
        'Car': {'national': 3000, 'international': 10000},
        'Flight': {'national': 6500, 'international': 32000},
        'Rental': {'national': 2500, 'international': 8000}
    }
    
    records = []
    
    for i in range(n_samples):
        # 65% national, 35% international
        is_international = np.random.rand() < 0.35
        trip_type = 'INTERNATIONAL' if is_international else 'NATIONAL'
        
        if is_international:
            destination = np.random.choice(international_destinations)
            # Duration typically 3 to 14 days
            duration_days = int(np.random.choice([3, 4, 5, 6, 7, 8, 10, 12, 14], p=[0.1, 0.15, 0.2, 0.2, 0.15, 0.08, 0.05, 0.04, 0.03]))
            # Transport usually Flight for international
            transport = np.random.choice(['Flight', 'Flight', 'Flight', 'Car', 'Train'], p=[0.75, 0.1, 0.05, 0.05, 0.05])
            travelers = int(np.random.choice([1, 2, 3, 4, 5], p=[0.3, 0.4, 0.15, 0.1, 0.05]))
            accommodation = np.random.choice(['Hotel', 'Resort', 'Hostel', 'Guesthouse', 'Luxury Hotel'], p=[0.45, 0.2, 0.15, 0.1, 0.1])
            daily_food_activity = np.random.uniform(3000, 6000)
        else:
            destination = np.random.choice(national_destinations)
            # Duration typically 2 to 10 days
            duration_days = int(np.random.choice([2, 3, 4, 5, 6, 7, 8, 10], p=[0.15, 0.25, 0.25, 0.15, 0.08, 0.05, 0.04, 0.03]))
            transport = np.random.choice(['Flight', 'Train', 'Bus', 'Car', 'Rental'], p=[0.35, 0.3, 0.15, 0.1, 0.1])
            travelers = int(np.random.choice([1, 2, 3, 4, 5, 6], p=[0.25, 0.4, 0.15, 0.12, 0.05, 0.03]))
            accommodation = np.random.choice(['Hotel', 'Resort', 'Hostel', 'Guesthouse', 'Luxury Hotel'], p=[0.5, 0.15, 0.15, 0.15, 0.05])
            daily_food_activity = np.random.uniform(1000, 2500)
            
        month = int(np.random.randint(1, 13))
        
        # Seasonality factor (Dec, Jan, May, Jun are high peak season)
        if month in [12, 1, 5, 6]:
            season_mult = np.random.uniform(1.15, 1.35)
        elif month in [7, 8]: # Monsoon / off-peak
            season_mult = np.random.uniform(0.85, 0.95)
        else:
            season_mult = np.random.uniform(0.98, 1.05)
            
        # Cost composition
        tier_key = 'international' if is_international else 'national'
        room_sharing_factor = max(1, (travelers + 1) // 2) # 2 travelers per room on average
        accom_cost = accommodation_tiers[accommodation][tier_key] * duration_days * room_sharing_factor * season_mult
        trans_cost = transport_bases[transport][tier_key] * travelers * season_mult
        activities_cost = daily_food_activity * duration_days * travelers
        
        # Total cost with realistic noise (+/- 6% real-world variance)
        raw_total = accom_cost + trans_cost + activities_cost
        noise = np.random.normal(0, raw_total * 0.05)
        total_cost = round(max(3000, raw_total + noise), 2)
        
        # Budget: planned user budget usually roughly correlates with expected cost
        budget_ratio = np.random.uniform(0.85, 1.25)
        budget = round(total_cost * budget_ratio / 500) * 500
        
        records.append({
            'destination': destination,
            'duration_days': duration_days,
            'travelers': travelers,
            'accommodation': accommodation,
            'transport': transport,
            'trip_type': trip_type,
            'month': month,
            'budget': budget,
            'total_cost': total_cost
        })
        
    df = pd.DataFrame(records)
    return df

if __name__ == '__main__':
    data_dir = os.path.join(os.path.dirname(__file__), '..', 'data')
    os.makedirs(data_dir, exist_ok=True)
    out_path = os.path.join(data_dir, 'travel_cost_dataset.csv')
    
    df = generate_travel_dataset(n_samples=1500, random_state=42)
    df.to_csv(out_path, index=False)
    print(f"Dataset generated with {len(df)} records at: {out_path}")
    print(df.head())
    print("\nSummary Statistics:")
    print(df.describe())
