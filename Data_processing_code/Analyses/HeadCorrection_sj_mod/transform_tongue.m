function avg_dist = transform_tongue(x,C1,C2)
 
theta = x(1);
xs = x(2);
ys = x(3);

% Old tongue is an m x 2 matrix of form [x(k) y(k)].
 
% Rotation of C2:
rotmat = [cos(theta),-sin(theta);sin(theta),cos(theta)];
C2_temp = rotmat*C2';

% Translation of C2:
[r,c] = size(C2_temp);
transmat = repmat([xs;ys],1,c);
C2_new = (C2_temp + transmat)';
%C1=C1
 
%C2_new = transform(theta,xs,ys);
 
avg_dist = curve_avg_distance(C1,C2_new);
